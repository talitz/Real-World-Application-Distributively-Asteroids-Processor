package main;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.List;


public class mySQS {
	    private static BasicAWSCredentials credentials;
	    private static AmazonSQS sqs;
	    private static volatile mySQS awssqsUtil = null;
	    private static String accessKey = null;
	    private static String secretKey = null;
	    
	    private mySQS() {
	    }

	    public static mySQS getInstance() {
		    	    if(awssqsUtil == null) {
				    		credentials = new BasicAWSCredentials(accessKey,secretKey);
				    		sqs = new AmazonSQSClient(credentials);
				    		awssqsUtil = new mySQS();
				    		new ClientConfiguration().setConnectionTimeout(0);
		    	    }
		    	    return awssqsUtil;
	    }
	    
	    public static void setAccessAndSecretKey(String accessKey,String secretKey) {
	    	System.out.println("mySQS :: accessKey & secretKey is now synchornized. you can work!\n");
	    	mySQS.accessKey = accessKey;
	    	mySQS.secretKey = secretKey;
	    }

	    public AmazonSQS getAWSSQSClient(){
	         return mySQS.sqs;
	    }

	    public String createQueue(String queueName){
	        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
	        String queueUrl = mySQS.sqs.createQueue(createQueueRequest).getQueueUrl();
	        return queueUrl;
	    }

	    public String getQueueUrl(String queueName){
	        GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(queueName);
	        return mySQS.sqs.getQueueUrl(getQueueUrlRequest).getQueueUrl();
	    }


	    public ListQueuesResult listQueues(){
	       return mySQS.sqs.listQueues();
	    }

	    public void sendMessageToQueue(String queueUrl, String message){
	        SendMessageResult messageResult =  mySQS.sqs.sendMessage(new SendMessageRequest(queueUrl, message));
	        System.out.println("mySQS :: sending message to queue " + queueUrl + ", content: " + message);
	        System.out.println("mySQS :: Message Body: " + messageResult + "\n");
	    }

	    public List<Message> getMessagesFromQueue(String queueUrl,String entity){
	       ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
	       List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
	       System.out.println("mySQS :: "+entity+" is getting waiting for results...");
	       return messages;
	    }
	    
	    public List<Message> awaitMessagesFromQueue(String queueUrl,int n,String entity){
		       ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		       receiveMessageRequest.setWaitTimeSeconds(n);
		       
		       List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		       System.out.println("mySQS ::  awaitMessagesFromQueue() was just called with terminate = " + Manager.isTerminate());
		       while(messages.isEmpty() && !Manager.isTerminate()) {
		    	     messages = sqs.receiveMessage(receiveMessageRequest).getMessages(); 
		    	     System.out.println("mySQS :: "+entity+" is still waiting for results...");
		       }
		       return messages;
		}
	    
	    public void printMessagesFromQueue(String queueUrl) {
	        List<Message> messages = getMessagesFromQueue(queueUrl,"");
	        
	        for(Message msg : messages) {
	            System.out.println(msg.getBody());
	        }
	    }
	    
	    public void deleteQueueByURL(String queueURL) {
	     	sqs.deleteQueue(queueURL);
	    }

	    public synchronized void deleteMessageFromQueue(String queueUrl, Message message){
	        String messageRecieptHandle = message.getReceiptHandle();
	        System.out.println("mySQS :: message deleted : " + message.getBody());
	        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
	    }
	}
