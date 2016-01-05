# ArikTools

ArikTools is a tool for secretly hiding messages in uncompressed or losslessly compressed images, such as PNGs, and a suite of several steganalytic tools for manually inspecting images.

The program is a canonical implementation of the XLSB steganographic algorithm, developed by the tool's original authors, which can hide large amounts of data in losslessly compressed images. It builds upon the canonical LSB approach, but unlike it, it is very careful to preserve a wide range of statistical properties, avoiding most (if not all) steganalytic techniques known at the time XLSB was originally developed (end of 2007).

## Introduction to Steganography and ArikTools

The core observation of most image-based stego techniques is that the human eye cannot distinguish small variations in color, and in fact, the last one or two (or even three) binary digits in a color of a pixel are just pure noise. Many compression techniques, such as JPG, are also based on similar observations. A color is usually represented as three 8-bit numbers corresponding to the red, green, and blue components - an 8-bit number is a number between 0 and 255. For example, navy blue is (0, 0, 128) - i.e. no red, no green, and half of the blue component. But in practice, if a human eye cannot distinguish between (0, 0, 128), (0, 0, 130), or even (0, 0, 124), even if they are side by side.

ArikTools provides a visualization for the last two binary digits of an image - when you load an image almost anywhere in the program, such as when clicking the "Encode any file" button, you can click the "Generate" button on the right in the "Low Order Bits" quarter and see the result. What you would see is the amplified noise in the low order bits of an image - more precisely, you will see the visualization of the remainder after division by 4 for each color component. It should look mostly random, but there are a few areas that would look not random.

We can hide information in an image by replacing the most random-looking parts this noise by an encrypted secret. This is called LSB cryptography. Normal LSB techniques just replace the noise by the hidden data, but it turns out that this is easily detectable. ArikTools implements three analysis methods that check statistical properties of pictures in order to detect hidden messages - they are the three buttons under "Automated Steganalysis".

The Wikipedia page on steganography (http://en.wikipedia.org/wiki/Steganography) contains examples of a few images with LSB-encoded messages, which you can download and load into the analyses. It will just print a bunch of scores, which I guess will not mean much to you, but if you then download a few similarly sized images with no encoded message, you will see that the steganalysis scores for LSB-encoded messages are significantly higher.

ArikTools' LSB encoding (which we call XLSB) uses a technique that avoids these detection methods as well. ArikTools encodes the message using addition and subtraction instead of binary operators, so that the resulting noise looks more natural.

But to be upfront about this, we believe that ArikTools was fairly secure back in 2007 when we developed it. This project is now practically abandoned, so we have not been following the steganalysis literature recently, so we do not know what new detection methods are out there. For example, there is a recent research that enables camera-identification based on the image noise, but we do not know how much threat it creates for stego tools.

## How to use ArikTools

Click "Encode any File" under "XLSB Steganography", then click "Load Cover Image" to load a PNG image. Underneath you will see the capacity that tells you how much information you can hide. This is the theoretical maximum - the less of it you use, the less detectable you are. In practice, you should be fine using as much as 1/4 of it without any problem. Then click "Load Secret File" to load the file to hide. Then you can click "Generate Encoded Image" to encode the file, and then you can save the encoded image (make sure to use PNG as the file type).

To decode, just use the "Decode a File" button. Please note that ArikTools does not preserve the file name, so you must somehow remember what it was!

If you are interested in using ArikTools techniques in a program, just take (or reimplement) the relevant functionality from ArikTools and put it into your application.

## Want it to really work?

ArikTools - or for that matter, almost any stego tool that works with images - works best for images that were never compressed. This means that the camera that you use to take pictures should either saw them as RAW, TIFF, or PNG - and the picture (or most of the picture) should be a photograph, not a computer art. The picture should never be in its lifetime stored as a JPG.

If you would like to get an intuition for why, you can go to "Image Filters" in ArikTools, load a JPG, and then click "Apply Filter". You will note then on the bottom right (the "Analysis" quarter) a lot of square-like patterns. If an image was ever a JPG, analyzing these square-like things can reveal if there is a hidden message.

But that being said - it all depends on the threat model. If you think steganalysis is unlikely, you can do whatever you want. If you are afraid of it, be wise in what pictures you choose for covers and how much information you hide in them.
