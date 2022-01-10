package testingpack;

import java.io.File;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

public class HighLevelMultipartUpload {

	public static void main(String[] args) throws Exception {

		Regions clientRegion = Regions.AP_SOUTH_1;
		String bucketName = "demo8jan2022";
		String keyName = "f2.csv";
		String filePath = "C:\\temp";
		String accessKey = " ";
		String secretKey = " ";

		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		try {

			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

			TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();

			// TransferManager processes all transfers asynchronously,
			// so this call returns immediately.
			Upload upload = tm.upload(bucketName, keyName, new File(filePath + "\\" + keyName));
			System.out.println("Object upload started");

			// Optionally, wait for the upload to finish before continuing.
			// upload.waitForCompletion();

			UploadResult r = upload.waitForUploadResult();

			System.out.println("r=" + r.getETag());
			System.out.println("Object upload complete");
			System.out.println("exiting..");

			boolean isCompleted = upload.isDone();

			if (isCompleted) {
				System.out.println("uload is completed  ");
				tm.shutdownNow();
			}

		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}
	}
}
