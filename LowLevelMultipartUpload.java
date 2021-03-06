package testingpack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;

public class LowLevelMultipartUpload {

	public static void main(String[] args) throws IOException {

		Regions clientRegion = Regions.AP_SOUTH_1;
		String bucketName = "demo8jan2022";
		String keyName = "f1.csv";
		String filePath = "C:\\temp";

		String accessKey=" ";
		String secretKey=" ";

		// Init your AmazonS3 credentials using BasicAWSCredentials.
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		File file = new File(filePath+"\\"+keyName);
		
		long contentLength = file.length();
		System.out.println("File getAbsolutePath details " + file.getAbsolutePath());
		System.out.println("File exists is " + file.exists());
		
		long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

		System.out.println("Starting file transfer at " + LocalDate.now());

		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

			// Create a list of ETag objects. You retrieve ETags for each object part
			// uploaded,
			// then, after each individual part has been uploaded, pass the list of ETags to
			// the request to complete the upload.
			List<PartETag> partETags = new ArrayList<PartETag>();

			// Initiate the multipart upload.
			InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);

			System.out.println("initRequest=" + initRequest.toString());

			InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);

			System.out.println("initResponse getUploadId=" + initResponse.getUploadId());

			System.out.println("contentLength=" + contentLength);
			System.out.println("Initial partSize=" + partSize);

			// Upload the file parts.
			long filePosition = 0;
			for (int i = 1; filePosition < contentLength; i++) {
				// Because the last part could be less than 5 MB, adjust the part size as
				// needed.
				partSize = Math.min(partSize, (contentLength - filePosition));

				System.out.println("starting , Part upload for i [partnum] =" + i);
				System.out.println("partSize=" + partSize);
				System.out.println("filePosition=" + filePosition);

				// Create the request to upload a part.
				UploadPartRequest uploadRequest = new UploadPartRequest().withBucketName(bucketName).withKey(keyName)
						.withUploadId(initResponse.getUploadId()).withPartNumber(i).withFileOffset(filePosition)
						.withFile(file).withPartSize(partSize);

				// Upload the part and add the response's ETag to our list.
				UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
				partETags.add(uploadResult.getPartETag());

				System.out.println("Ending , Part upload for i [partnum] = " + i);
				System.out.println("uploadRequest=" + uploadRequest.toString());
				System.out.println("uploadResult=" + uploadResult.toString());
				System.out.println("partETags=" + partETags);

				filePosition += partSize;
			}

			// Complete the multipart upload.
			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, keyName,
					initResponse.getUploadId(), partETags);

			CompleteMultipartUploadResult res = s3Client.completeMultipartUpload(compRequest);
			System.out.println("res=" + res.toString());

			System.out.println("Ended file transfer at " + LocalDate.now());

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
