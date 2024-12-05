package com.cae;


import io.minio.BucketExistsArgs;
        import io.minio.MakeBucketArgs;
        import io.minio.MinioClient;
        import io.minio.UploadObjectArgs;
        import io.minio.errors.MinioException;
        import java.io.IOException;
        import java.security.InvalidKeyException;
        import java.security.NoSuchAlgorithmException;

public class test {
    public static void main(String[] args)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("https://222.27.255.211:19000")
                            .credentials("fileadmin_test", "fileadmin")
                            .build();

            // Make 'asiatrip' bucket if not exist.
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("HULL-MODEL-AND-INFORMATION-DB").build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("HULL-MODEL-AND-INFORMATION-DB").build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }

            // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
            // 'asiatrip'.
            /*minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("asiatrip")
                            .object("asiaphotos-2015.zip")
                            .filename("/home/user/Photos/asiaphotos.zip")
                            .build());
            System.out.println(
                    "'/home/user/Photos/asiaphotos.zip' is successfully uploaded as "
                            + "object 'asiaphotos-2015.zip' to bucket 'asiatrip'.");*/
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }
}