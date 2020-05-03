package fr.amanin.stackoverflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.stream.Stream;

public class YAMLOpenCV {
    public static final ObjectMapper OPENCV_YAML_MAPPER = new YAMLMapper();

    public static void main(String[] args) throws Exception {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        final String confStr =
                "cameraMatrix: !!opencv-matrix\n" +
                        "   rows: 3\n" +
                        "   cols: 3\n" +
                        "   dt: d\n" +
                        "   data: [ 6.6278599887122368e+02, 0., 3.1244256016006659e+02, 0.,\n" +
                        "       6.6129276875199082e+02, 2.2747179767124251e+02, 0., 0., 1. ]\n" +
                        "imageSize_width: 640\n" +
                        "imageSize_height: 480\n" +
                        "sensorSize_width: 0\n" +
                        "sensorSize_height: 0\n" +
                        "distCoeffs: !!opencv-matrix\n" +
                        "   rows: 5\n" +
                        "   cols: 1\n" +
                        "   dt: d\n" +
                        "   data: [ -1.8848338341464690e-01, 1.0721890419183855e+00,\n" +
                        "       -3.5244467228016116e-03, -7.0195032848241403e-04,\n" +
                        "       -2.0412827999027101e+00 ]\n" +
                        "reprojectionError: 2.1723265945911407e-01";
        OpenCVConfig conf = OPENCV_YAML_MAPPER.readValue(confStr, OpenCVConfig.class);
        System.out.println(conf);

        String serialized = OPENCV_YAML_MAPPER.writeValueAsString(conf);
        System.out.println(serialized);
    }

    public static class OpenCVConfig {
        int imageSize_width;
        int imageSize_height;
        int sensorSize_width;
        int sensorSize_height;

        double reprojectionError;

        @JsonDeserialize(converter = ToMatConverter.class)
        @JsonSerialize(converter = FromMatConverter.class)
        Mat cameraMatrix;
        @JsonDeserialize(converter = ToMatConverter.class)
        @JsonSerialize(converter = FromMatConverter.class)
        Mat distCoeffs;

        public int getImageSize_width() {
            return imageSize_width;
        }

        public OpenCVConfig setImageSize_width(int imageSize_width) {
            this.imageSize_width = imageSize_width;
            return this;
        }

        public int getImageSize_height() {
            return imageSize_height;
        }

        public OpenCVConfig setImageSize_height(int imageSize_height) {
            this.imageSize_height = imageSize_height;
            return this;
        }

        public int getSensorSize_width() {
            return sensorSize_width;
        }

        public OpenCVConfig setSensorSize_width(int sensorSize_width) {
            this.sensorSize_width = sensorSize_width;
            return this;
        }

        public int getSensorSize_height() {
            return sensorSize_height;
        }

        public OpenCVConfig setSensorSize_height(int sensorSize_height) {
            this.sensorSize_height = sensorSize_height;
            return this;
        }

        public double getReprojectionError() {
            return reprojectionError;
        }

        public OpenCVConfig setReprojectionError(double reprojectionError) {
            this.reprojectionError = reprojectionError;
            return this;
        }

        public Mat getCameraMatrix() {
            return cameraMatrix;
        }

        public OpenCVConfig setCameraMatrix(Mat cameraMatrix) {
            this.cameraMatrix = cameraMatrix;
            return this;
        }

        public Mat getDistCoeffs() {
            return distCoeffs;
        }

        public OpenCVConfig setDistCoeffs(Mat distCoeffs) {
            this.distCoeffs = distCoeffs;
            return this;
        }

        @Override
        public String toString() {
            return "OpenCVConfig{" +
                    "imageSize_width=" + imageSize_width +
                    ", imageSize_height=" + imageSize_height +
                    ", sensorSize_width=" + sensorSize_width +
                    ", sensorSize_height=" + sensorSize_height +
                    ", camerMatrix=" + cameraMatrix +
                    ", distCoeffs=" + distCoeffs +
                    '}';
        }
    }

    private static class FromMatConverter implements Converter<Mat, Matrix> {

        @Override
        public Matrix convert(Mat value) {
            final Matrix result = new Matrix();
            result.cols = value.cols();
            result.rows = value.rows();
            final int type = value.type();
            result.dt = Stream.of(MatrixDataType.values())
                    .filter(dt -> dt.mapping == type)
                    .findAny()
                    .orElseThrow(() -> new UnsupportedOperationException("No matching datatype found for "+type));
            int idx = 0;
            result.data = new double[result.rows * result.cols];
            for (int r = 0 ; r < result.rows ; r++) {
                for (int c = 0; c < result.cols; c++) {
                    final double[] v = value.get(r, c);
                    result.data[idx++] = v[0];
                }
            }
            return result;
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(new TypeReference<Mat>() {});
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(new TypeReference<Matrix>() {});
        }
    }

    private static class ToMatConverter implements Converter<Matrix, Mat> {

        @Override
        public Mat convert(Matrix in) {
            final Mat result = new Mat(in.rows, in.cols, in.dt.mapping);

            int idx = 0;
            for (int r = 0 ; r < in.rows ; r++) {
                for (int c = 0; c < in.cols; c++) {
                    result.put(r, c, in.data[idx++]);
                }
            }

            return result;
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(new TypeReference<Matrix>() {});
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(new TypeReference<Mat>() {});
        }
    }

    public static class Matrix {
        int rows;
        int cols;
        MatrixDataType dt;
        double[] data;

        public int getRows() {
            return rows;
        }

        public Matrix setRows(int rows) {
            this.rows = rows;
            return this;
        }

        public int getCols() {
            return cols;
        }

        public Matrix setCols(int cols) {
            this.cols = cols;
            return this;
        }

        public MatrixDataType getDt() {
            return dt;
        }

        public Matrix setDt(MatrixDataType dt) {
            this.dt = dt;
            return this;
        }

        public double[] getData() {
            return data;
        }

        public Matrix setData(double[] data) {
            this.data = data;
            return this;
        }

        double at(int x, int y) {
            if (x >= cols || y >= rows) throw new IllegalArgumentException("Bad coordinate");
            return data[y*rows + x];
        }

        @Override
        public String toString() {
            return "Matrix{" +
                    "rows=" + rows +
                    ", cols=" + cols +
                    ", dt=" + dt +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }
/*
    public static class MatDeserializer extends StdDeserializer<Mat> {

        protected MatDeserializer() {
            super(Mat.class);
        }

        @Override
        public Mat deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final int rows, cols;
            final MatrixDataType dtype;
            final double[] data;
        }
    }

    public static class MatSerializer extends StdSerializer<Mat> {

        protected MatSerializer() {
            super(Mat.class);
        }

        @Override
        public void serialize(Mat value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumberField("rows", value.rows());
            gen.writeNumberField("cols", value.cols());
            gen.writeFieldName("data");
            gen.writeStartArray();
            gen.writeEndArray();
        }
    }
*/
    public enum MatrixDataType {
        d(CvType.CV_64F),
        f(CvType.CV_32F);

        public final int mapping;
        MatrixDataType(int mapping) {
            this.mapping = mapping;
        }
    }
}
