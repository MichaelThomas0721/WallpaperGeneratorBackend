package com.thomas.WallpaperGenerator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Image;

public class GifProcessor {

    final BigDecimal gapRatio = BigDecimal.valueOf(12).divide(BigDecimal.valueOf(50));

    public File[] DivideFile(File startingGif) throws FileNotFoundException, IOException {

        final FileInputStream data = new FileInputStream(startingGif);
        final GifImage gif = GifDecoder.read(data);
        final int width = gif.getWidth();
        final int height = gif.getHeight();
        final int background = gif.getBackgroundColor();
        final int frameCount = gif.getFrameCount();
        ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();

        final int rows = 3;
        final int columns = 5;
        final int finalSize = 72;

        File[] endingGifs = new File[rows * columns];

        ArrayList<BufferedImage>[][] gifArrays = new ArrayList[rows][columns];

        BigDecimal imageRatio = BigDecimal.valueOf(gif.getFrame(0).getHeight())
                .divide(BigDecimal.valueOf(gif.getFrame(0).getWidth()), 10, RoundingMode.HALF_UP);

        BigDecimal finalImageRatio = RatioCalculator(BigDecimal.valueOf(rows))
                .divide(RatioCalculator(BigDecimal.valueOf(columns)), 10, RoundingMode.HALF_UP);

        int res = imageRatio.compareTo(finalImageRatio);

        BigDecimal size = BigDecimal.valueOf(0);

        if (res == 1) {
            size = BigDecimal.valueOf((25 * gif.getFrame(0).getWidth()) / ((31 * columns) - 6));
        } else if (res == -1) {
            size = BigDecimal.valueOf((25 * gif.getFrame(0).getHeight()) / ((31 * rows) - 6));
        } else {

        }
        int imageCount = 0;
        for (int boxes = 0; boxes < gifArrays.length; boxes++) {
            for (int gifImages = 0; gifImages < gifArrays[boxes].length; gifImages++) {
                gifArrays[boxes][gifImages] = new ArrayList<BufferedImage>();
                for (int i = 0; i < frameCount; i++) {
                    final BufferedImage img = gif.getFrame(i);
                    final BufferedImage dest = img.getSubimage(
                            (size.multiply(BigDecimal.valueOf(gifImages))
                                    .add((BigDecimal.valueOf(gifImages).multiply(size.multiply(gapRatio)))))
                                            .intValue(),
                            (size.multiply(BigDecimal.valueOf(boxes))
                                    .add((BigDecimal.valueOf(boxes).multiply(size.multiply(gapRatio))))).intValue(),
                            size.intValue(), size.intValue());
                    BufferedImage resizedImage = new BufferedImage(finalSize, finalSize, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(dest, 0, 0, finalSize, finalSize, null);
                    final int delay = gif.getDelay(i);
                    gifArrays[boxes][gifImages].add(resizedImage);
                }

                ImageWriter iw = ImageIO.getImageWritersByFormatName("gif").next();

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                iw.setOutput(ios);
                iw.prepareWriteSequence(null);
                int i2 = 0;

                for (int i = 1; i < gifArrays[boxes][gifImages].size(); i++) {

                    BufferedImage src = gifArrays[boxes][gifImages].get(i);
                    ImageWriteParam iwp = iw.getDefaultWriteParam();
                    IIOMetadata metadata = iw.getDefaultImageMetadata(
                            new ImageTypeSpecifier(src), iwp);

                    String metaFormatName = metadata.getNativeMetadataFormatName();

                    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
                    IIOMetadataNode graphicsControlExtensionNode = getNode(
                            root,
                            "GraphicControlExtension");
                    graphicsControlExtensionNode.setAttribute(
                            "delayTime",
                            Integer.toString(gif.getDelay(i)));
                    graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
                    graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
                    graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");

                    IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
                    commentsNode.setAttribute("CommentExtension", "Created by: https://memorynotfound.com");

                    IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
                    IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
                    child.setAttribute("applicationID", "NETSCAPE");
                    child.setAttribute("authenticationCode", "2.0");

                    int loopContinuously = true ? 0 : 1;
                    child.setUserObject(new byte[] { 0x1, (byte) (loopContinuously & 0xFF),
                            (byte) ((loopContinuously >> 8) & 0xFF) });
                    appExtensionsNode.appendChild(child);
                    metadata.setFromTree(metaFormatName, root);
                    // configure(metadata, "" + 100, i2);

                    IIOImage ii = new IIOImage(src, null, metadata);
                    iw.writeToSequence(ii, null);
                    i2++;
                }

                iw.endWriteSequence();
                ios.close();

                OutputStream out = new FileOutputStream("Frames\\GifFrame_" + imageCount + ".gif");
                new File("./FramesTest").mkdir();
                File testFile = new File("FramesTest\\GifFrame_" + imageCount + ".gif");
                FileOutputStream testOut = new FileOutputStream(testFile);
                imageCount++;
                out.write(os.toByteArray());
                testOut.write(os.toByteArray());
                out.flush();
                out.close();
                testOut.flush();
                testOut.close();
                endingGifs[imageCount - 1] = testFile;
            }
        }

        return endingGifs;
    }

    private BigDecimal RatioCalculator(BigDecimal num) {
        return (num.add(((num.subtract(BigDecimal.valueOf(1)).multiply(gapRatio)))));
    }

    private IIOMetadataNode getNode(
            IIOMetadataNode rootNode,
            String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }
}