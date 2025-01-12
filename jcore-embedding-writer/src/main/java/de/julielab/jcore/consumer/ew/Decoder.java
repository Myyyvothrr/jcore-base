package de.julielab.jcore.consumer.ew;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * This class offers methods to decode the binary format of a sequence of text-embedding pairs. It also offers code
 * for merging multiple streams of ordered text-embedding pair sequences in binary format into a single output
 * where for each text occurrence, all its embedding vectors are averaged.
 */
public class Decoder {

    public static Pair<List<String>, List<double[]>> decodeBinaryEmbeddingVectors(InputStream is) throws IOException {
        return decodeBinaryEmbeddingVectors(is, 8192);
    }

    public static Pair<List<String>, List<double[]>> decodeBinaryEmbeddingVectors(InputStream is, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        byte[] currentText;
        double[] currentVector;
        byte[] integerBuffer = new byte[Integer.BYTES];
        byte[] doubleBuffer = new byte[Double.BYTES];

        List<String> expressions = new ArrayList<>();
        List<double[]> vectors = new ArrayList<>();

        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            bb.limit(bytesRead);
            while (bb.position() < bb.limit()) {
                Pair<String, double[]> pair = getNextTextEmbeddingPair(is, bb, integerBuffer, doubleBuffer);
                expressions.add(pair.getLeft());
                vectors.add(pair.getRight());

            }
        }
        return new ImmutablePair<>(expressions, vectors);
    }

    private static Pair<String, double[]> getNextTextEmbeddingPair(InputStream is, ByteBuffer bb, byte[] integerBuffer, byte[] doubleBuffer) throws IOException {
        byte[] currentText;
        double[] currentVector;
        final int textLength = readInt(integerBuffer, bb, is);
        currentText = new byte[textLength];
        readNumberOfBytes(currentText, bb, is);
        final String text = new String(currentText, StandardCharsets.UTF_8);

        final int vectorLength = readInt(integerBuffer, bb, is);
        currentVector = new double[vectorLength];
        for (int i = 0; i < vectorLength; i++) {
            currentVector[i] = readDouble(doubleBuffer, bb, is);
        }
        return new ImmutablePair<>(text, currentVector);
    }

    /**
     * Merge multiple InputStreams of byte encoded text-embeddingvector pairs into a single OutputStream.
     * All sources are expected to be sorted lexicographically ascending with regards to the text elements.
     * If <tt>groupByText</tt> is set to <tt>true</tt>, the output will be grouped by the text. Each text
     * element will then only exist once in the output and all its originally associated vectors will be collapsed
     * into one averaged vector for this text.
     *
     * @param inputStreams The input streams holding the text-vector pairs in byte format.
     * @param os           The output stream to write the text-vector pairs in byte format to.
     * @param groupByText  If the vectors associated with the same text should be averaged into a single vector, a centroid.
     * @throws IOException
     */
    public static void mergeEmbeddingFiles(List<InputStream> inputStreams, OutputStream os, boolean groupByText) throws IOException {
        // Define the data structures we need
        List<Pair<String, double[]>> outputBuffer = new ArrayList<>();
        List<Pair<String, double[]>> currentStreamValues = new ArrayList<>(inputStreams.size());
        List<ByteBuffer> streamBuffers = new ArrayList<>(inputStreams.size());
        byte[] integerBuffer = new byte[Integer.BYTES];
        byte[] doubleBuffer = new byte[Double.BYTES];
        // Initialize the lists
        for (InputStream ignore : inputStreams)
            streamBuffers.add(ByteBuffer.allocate(8192));
        // Read the first vectors from all streams
        for (int i = 0; i < inputStreams.size(); i++) {
            final InputStream is = inputStreams.get(i);
            final ByteBuffer bb = streamBuffers.get(i);
            // The byte buffers are empty at this point. The method we call next assumed that
            // the byte buffers contain data from the input stream.
            // By setting the buffers to appear completely read, we cause the subsequently called method
            // to read data from the input stream into the byte buffer first.
            bb.position(bb.capacity());
            try {
                final Pair<String, double[]> pair = getNextTextEmbeddingPair(is, bb, integerBuffer, doubleBuffer);
                currentStreamValues.add(pair);
            } catch (NoSuchElementException e) {
                currentStreamValues.add(null);
            }
        }

        // Now always advance the stream that currently has the lexicographically "smallest" text part.
        // When we are not in groupByText mode, we will just write the text-vector pairs in this order to the
        // output stream, having merged the streams while keeping them ordered.
        // If we are in groupByText mode, we will also collapse the vectors that have the same text part
        // by averaging over them and only writing the averaged vector to the output stream. This would be the final
        // result of the whole processing: For each text we then have the centroid of all its embedding vectors.
        int numExhaustedStreams = 0;
        ByteBuffer outputBb = ByteBuffer.allocate(8192);
        // We save the next vector pair to write only for the purpose to write the very last vector to the output,
        // after all streams have been exhausted.
        Pair<String, double[]> nextVectorPair = null;
        while (numExhaustedStreams < inputStreams.size()) {
            int minIndex = -1;
            String min = null;
            // Find the stream with the currently smallest string value, lexicographically
            for (int i = 0; i < currentStreamValues.size(); i++) {
                final Pair<String, double[]> streamValue = currentStreamValues.get(i);
                if (streamValue != null) {
                    if (min == null || streamValue.getLeft().compareTo(min) < 0) {
                        min = streamValue.getLeft();
                        minIndex = i;
                    }
                }
            }
            if (minIndex != -1) {
                final Pair<String, double[]> minValue = currentStreamValues.get(minIndex);
                // Check if we arrived at the next text. If so, we will write the current output buffer contents
                // to the output stream, averaging the vectors first if we are in groupByTextMode
                writeTextEmbeddingPairToOutputStream(minValue, outputBuffer, os, outputBb, groupByText);
                // Either we have written something out or not, continue to add to the output buffer until
                // the next change of the text part happens
                outputBuffer.add(minValue);
                // Now get the text-vector pair from the InputStream we just read from, setting null to the
                // current stream values for this stream if it is now exhausted.
                InputStream minStream = inputStreams.get(minIndex);
                ByteBuffer minBb = streamBuffers.get(minIndex);
                try {
                    nextVectorPair = getNextTextEmbeddingPair(minStream, minBb, integerBuffer, doubleBuffer);
                } catch (NoSuchElementException e) {
                    nextVectorPair = null;
                }
                currentStreamValues.set(minIndex, nextVectorPair);
            }
            // Check if there still is a stream with values or if all values are null now.
            // If there is still a value, continue on top of the loop with the next smallest value.
            numExhaustedStreams = (int) currentStreamValues.stream().filter(Objects::isNull).count();
        }
        writeTextEmbeddingPairToOutputStream(null, outputBuffer, os, outputBb, groupByText);


    }

    private static void writeTextEmbeddingPairToOutputStream(Pair<String, double[]> textEmbeddingPair, List<Pair<String, double[]>> outputBuffer, OutputStream os, ByteBuffer outputBb, boolean groupByText) throws IOException {
        if (!outputBuffer.isEmpty() && (textEmbeddingPair == null || !outputBuffer.get(outputBuffer.size() - 1).getLeft().equals(textEmbeddingPair.getLeft()))) {
            if (groupByText) {
                double[] avgVector = VectorOperations.getAverageEmbeddingVector(outputBuffer.stream().map(Pair::getRight));
                String text = outputBuffer.get(0).getLeft();
                os.write(Encoder.encodeTextVectorPair(text, avgVector, outputBb));
            } else {
                for (Pair<String, double[]> p : outputBuffer)
                    os.write(Encoder.encodeTextVectorPair(p, outputBb));
            }
            outputBuffer.clear();
        }
    }

    /**
     * Reads a double from <tt>bb</tt> with reloading from <tt>is</tt> if necessary.
     *
     * @param dest An {@link Double#BYTES} sized array to hold the double bytes.
     * @param bb   A ByteBuffer that is used to buffer input from <tt>is</tt>.
     * @param is   The original InputStream that is read.
     * @return The read double value.
     * @throws IOException If reading fails.
     */
    public static double readDouble(byte[] dest, ByteBuffer bb, InputStream is) throws IOException {
        readNumberOfBytes(dest, bb, is);
        // from https://stackoverflow.com/a/31637248/1314955
        // Makes sense: a double encoded two information, the exponent and the mantissa (significant), both as
        // an integer of itself
        int upper = (((dest[0] & 0xff) << 24)
                + ((dest[1] & 0xff) << 16)
                + ((dest[2] & 0xff) << 8) + ((dest[3] & 0xff) << 0));
        int lower = (((dest[4] & 0xff) << 24)
                + ((dest[5] & 0xff) << 16)
                + ((dest[6] & 0xff) << 8) + ((dest[7] & 0xff) << 0));
        return Double.longBitsToDouble((((long) upper) << 32)
                + (lower & 0xffffffffl));
    }

    /**
     * Reads an integer from <tt>bb</tt> with reloading from <tt>is</tt> if necessary.
     *
     * @param dest An {@link Integer#BYTES} sized array to hold the integer bytes.
     * @param bb   A ByteBuffer that is used to buffer input from <tt>is</tt>.
     * @param is   The original InputStream that is read.
     * @return The read integer.
     * @throws IOException If reading fails.
     */
    public static int readInt(byte[] dest, ByteBuffer bb, InputStream is) throws IOException {
        readNumberOfBytes(dest, bb, is);
        return ((dest[0] & 0xff) << 24) | ((dest[1] & 0xff) << 16) | ((dest[2] & 0xff) << 8) | (dest[3] & 0xff);
    }

    /**
     * Reads bytes from <tt>bb</tt> until <tt>dest</tt> is full. If <tt>bb</tt> is exhausted before <tt>dest</tt>
     * could be filled, bytes from <tt>is</tt> are read into the backing byte[] of <tt>bb</tt> and the position
     * of <tt>bb</tt> is set to 0 and reading continues until <tt>dest</tt> is full or there are no more bytes
     * available from <tt>is</tt>.
     *
     * @param dest The destination array to fill from the InputStream through the given ByteBuffer.
     * @param bb   A ByteBuffer that may contain already read contents from <tt>is</tt>.
     * @param is   The original input stream from which bytes are read into <tt>bb</tt> for further consumption.
     * @throws IOException If reading from <tt>is</tt> fails.
     */
    public static void readNumberOfBytes(byte[] dest, ByteBuffer bb, InputStream is) throws IOException {
        int bytesRead = 0;
        while (bytesRead < dest.length) {
            for (; bb.position() < bb.limit() && bytesRead < dest.length; bytesRead++) {
                dest[bytesRead] = bb.get();
            }
            // check if we could read the whole integer. If not, we have exhausted the current buffer and need
            // to read the next chunk of data
            if (bytesRead < dest.length) {
                // read the next chunk of data right into the array wrapped by the passes ByteBuffer.
                // Then reset the ByteBuffer's position so we can start reading from it
                int numRead;
                if ((numRead = is.read(bb.array())) == -1)
                    throw new NoSuchElementException("The input stream does not offer enough bytes to fill the passed destination array.");

                bb.position(0);
                bb.limit(numRead < 0 ? 0 : numRead);
            }
        }
    }


}
