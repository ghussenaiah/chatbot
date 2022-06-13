package com.microsoft.teams.app;


import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.InvokeResponse;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.Attachment;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.CardImage;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.HeroCard;
import com.microsoft.bot.schema.ResourceResponse;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.ThumbnailCard;
import com.microsoft.bot.schema.teams.AppBasedLinkQuery;
import com.microsoft.bot.schema.teams.MessagingExtensionAttachment;
import com.microsoft.bot.schema.teams.MessagingExtensionResponse;
import com.microsoft.bot.schema.teams.MessagingExtensionResult;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.core.CustomRequestBuilder;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.models.ChatMessage;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.ResponseType;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.requests.ChatMessageCollectionPage;
import com.microsoft.graph.requests.DriveCollectionPage;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemRequestBuilder;
import com.microsoft.graph.requests.DriveRecentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.SiteCollectionPage;
import com.microsoft.teams.app.entity.AutoGenarationCode;
import com.microsoft.teams.app.entity.ChatHistory_299;
import com.microsoft.teams.app.entity.Department_23;
import com.microsoft.teams.app.entity.Ticket_296;
import com.microsoft.teams.app.repository.AutoGenerationRepo;
import com.microsoft.teams.app.repository.TicketRepo;
import com.microsoft.teams.app.service.impl.DepartmentImpl;
import com.microsoft.teams.app.service.impl.SupportImpl;

import okhttp3.Request;

import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;



/*


spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL55Dialect
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/newtest?useSSL=false
spring.datasource.username=root
spring.datasource.password=WANAparthy@544
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
*/

public class EchoBot extends TeamsActivityHandler {
	
	
	
	
	
	
	private Logger logger = LoggerFactory.getLogger(BotController.class);
	

	
	@Autowired
	DepartmentImpl departmentImpl;
	
	@Autowired
	SupportImpl supportImpl;
	
	@Autowired
	SupportService supportService;
	
	@Autowired
	DepartmentService depService;
	
	@Autowired 
	TicketService ticketService;
	
	@Autowired 
	CommonUtility commonUtility;
	
	@Autowired 
	TicketQualityService ticketQualityService;
	
	@Autowired
	TicketRepo ticketRepo;
	
	@Autowired
	AutoGenerationRepo autoGenerationRepo;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	

	@Autowired
	FetchnSave_Retry_Mechanism fetchnSave_Retry_Mechanism;
	
	
	
	public ConcurrentHashMap<String, Ticket_296> ticket = new ConcurrentHashMap<>();
	/*
	 * @PostConstruct public void test() {
	 * 
	 * List<Department_23> departmentList = departmentImpl.findAll();
	 * System.out.println(departmentList);
	 * 
	 * }
	 */
	
	    @PostConstruct 
		public void test() throws Exception {
	    	
	    	
	    	
	    	
	    	
	    	String clientId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
	    	String[] scope = {"files.readwrite.all", "offline_access"};
	    	String redirectURL = "http://localhost:8080/";
	    	String clientSecret = "xxxxxxxxxxxxxxxxxxxxxxx";
	
	    
	    	
	     //   URL url = new URL("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Employee%20Declaration%20_Income%20tax_Form.xlsx");
	     //   HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();		
	      //  String accessToken="eyJ0eXAiOiJKV1QiLCJub25jZSI6IjB6TXdKYVFCV3ZLRzdWZHpYZUV6Y20xLWZaT213aGhlYjJhSUlybmpwLTQiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC85MzYyYzI2NC1mNGNmLTQ1ZDMtOGU2MC1lZjg4Y2ViNzM2MDMvIiwiaWF0IjoxNjU1MDUyMzY5LCJuYmYiOjE2NTUwNTIzNjksImV4cCI6MTY1NTA1NjcwMiwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhUQUFBQXU0WnQwZGtnbjd5OUx4b0FFRWZZbC80S09YZ0xaZit0VEFIb3p0aUd6R0E9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJHcmFwaCBFeHBsb3JlciIsImFwcGlkIjoiZGU4YmM4YjUtZDlmOS00OGIxLWE4YWQtYjc0OGRhNzI1MDY0IiwiYXBwaWRhY3IiOiIwIiwiZmFtaWx5X25hbWUiOiJJbmRpYSIsImdpdmVuX25hbWUiOiJLYWdhbWkiLCJpZHR5cCI6InVzZXIiLCJpcGFkZHIiOiIyNy42LjEzMy4yMTciLCJuYW1lIjoiS2FnYW1pIEluZGlhIiwib2lkIjoiMTNlZDIxODAtNmIxYi00Y2ZkLTk1ZmYtZjMyZmU1MTcyYTQ2IiwicGxhdGYiOiI4IiwicHVpZCI6IjEwMDMyMDAxNTJCQjU1NjgiLCJyaCI6IjAuQVhBQVpNSmlrOF8wMDBXT1lPLUl6cmMyQXdNQUFBQUFBQUFBd0FBQUFBQUFBQUJ3QUVNLiIsInNjcCI6IkFwcGxpY2F0aW9uLlJlYWQuQWxsIEFwcGxpY2F0aW9uLlJlYWRXcml0ZS5BbGwgQ2hhbm5lbE1lc3NhZ2UuUmVhZC5BbGwgQ2hhdC5DcmVhdGUgQ2hhdC5SZWFkV3JpdGUgRGlyZWN0b3J5LlJlYWQuQWxsIERpcmVjdG9yeS5SZWFkV3JpdGUuQWxsIEdyb3VwLlJlYWQuQWxsIEdyb3VwLlJlYWRXcml0ZS5BbGwgTWFpbC5SZWFkIE1haWwuUmVhZEJhc2ljIE1haWwuUmVhZFdyaXRlIG9wZW5pZCBQZW9wbGUuUmVhZCBQZW9wbGUuUmVhZC5BbGwgcHJvZmlsZSBUZWFtLkNyZWF0ZSBUZWFtLlJlYWRCYXNpYy5BbGwgVGVhbU1lbWJlci5SZWFkV3JpdGUuQWxsIFRlYW1zQXBwSW5zdGFsbGF0aW9uLlJlYWRGb3JDaGF0IFRlYW1zQXBwSW5zdGFsbGF0aW9uLlJlYWRGb3JUZWFtIFRlYW1zQXBwSW5zdGFsbGF0aW9uLlJlYWRGb3JVc2VyIFRlYW1zQXBwSW5zdGFsbGF0aW9uLlJlYWRXcml0ZUZvckNoYXQgVGVhbXNBcHBJbnN0YWxsYXRpb24uUmVhZFdyaXRlRm9yVGVhbSBUZWFtc0FwcEluc3RhbGxhdGlvbi5SZWFkV3JpdGVTZWxmRm9yQ2hhdCBUZWFtU2V0dGluZ3MuUmVhZC5BbGwgVGVhbVNldHRpbmdzLlJlYWRXcml0ZS5BbGwgVXNlci5SZWFkIFVzZXIuUmVhZC5BbGwgVXNlci5SZWFkQmFzaWMuQWxsIFVzZXIuUmVhZFdyaXRlLkFsbCBlbWFpbCIsInNpZ25pbl9zdGF0ZSI6WyJrbXNpIl0sInN1YiI6ImFsT0xLSlpaMVh4Z0Fib2Y2ZDF3TzN4X2toTTNyZ2swQWJTb2VydFl2YVkiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiQVMiLCJ0aWQiOiI5MzYyYzI2NC1mNGNmLTQ1ZDMtOGU2MC1lZjg4Y2ViNzM2MDMiLCJ1bmlxdWVfbmFtZSI6ImFkbWluQGtnbWlwLm9ubWljcm9zb2Z0LmNvbSIsInVwbiI6ImFkbWluQGtnbWlwLm9ubWljcm9zb2Z0LmNvbSIsInV0aSI6IjAzdWxueUdQY2s2QVBqNUN4Mmk3QUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbIjYyZTkwMzk0LTY5ZjUtNDIzNy05MTkwLTAxMjE3NzE0NWUxMCIsImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfc3QiOnsic3ViIjoibkdmai1BNkpDS011czhxTlZCalZRSlg1TUwxWmRsV1I3NnlBMmJGbHhkcyJ9LCJ4bXNfdGNkdCI6MTYyMzg2MzU0OX0.r1vynk2zAW0So5OuvbraADogtt1I_cJBRGBeAsmywTN9LI5BwODDCXNKxVHbQ2r33MIkx6iMC23NmKWyExznFWY7SGPP4-Jb41vwXp9dbu4W-Ph9Qxf69vZmk6mHPh8gzTTxHnh2CFNopvI3UxXV2UyJVqnpnBGQE2PerTjWWlGq3qFKyPyrZNwR1gnlS7n8BVC8j7nKNWkpVi_p2AppE-6RMqIO7GFQNtlIkDDOz8FPX09MtMfc7t88OhkleAJyfdr691ctZRLRIcwcO7hDcCqYlLCEHgIA1HEofREBXR9eeifVAfqD7bbiQcURiqpHqN5uqA03Z13T4KqJlyJuMw";
	      //  httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);
	     //   int responseCode = httpConn.getResponseCode();
	    //	System.out.println(responseCode);
	    	
	      //  fetchnSave_Retry_Mechanism.updateTeamsMsgToDatabase();
	    	BufferedInputStream bis = null;
	    	BufferedOutputStream bos = null;
	    	
	    	
	    	final UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
					.clientId("dc3cba5c-d9f7-4a84-b6f6-f51d07a20480").username("admin@kgmip.onmicrosoft.com")
					.password("Kgm@123$").build();
			final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(
					usernamePasswordCredential);
			System.out.println("hello world");

			final GraphServiceClient<Request> graphClient = GraphServiceClient.builder()
					.authenticationProvider(tokenCredentialAuthProvider).buildClient();
			
			
			
			
			//  + "/$value"
			
			String baseUrl="https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Employee%20Declaration%20_Income%20tax_Form.xlsx";
			
			

			 InputStream stream12 = graphClient.customRequest(baseUrl, InputStream.class)
		    			.buildRequest()
		    			.get();
			
			
			//final InputStream result = graphClient.me().messages("id").attachments().buildRequest();
			final CustomRequestBuilder downloadRequestBuilder = new CustomRequestBuilder<>(baseUrl, graphClient, new ArrayList(), InputStream.class);
			final InputStream stream = (InputStream) downloadRequestBuilder.buildRequest().get();
			
		com.microsoft.graph.models.Attachment ath= graphClient.me().messages("1654879856327").attachments("e68f4cd2-dc1f-47b9-9f4b-29d7c86ee8d5").buildRequest().get();
		System.out.println(ath);
	    	
	    	
	    // https://135.181.202.86:12002/kagami-generated_Srinivasa_Live/dms/downloadDocument?docId=1654509082354
	    	 //  URL url = new URL("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Screenshot%20from%202022-06-05%2022-28-36.png");
	    	   // URL url = new URL("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Employee%20Declaration%20_Income%20tax_Form1212.xlsx");
	    //	URL url = new URL("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft Teams Chat Files/Screenshot from 2022-06-05 22-28-36.png");
	    	  //  URL url = new URL("https://www.w3schools.com/css/img_5terre.jpg");
	    	    

	    	//BufferedReader reader = new BufferedReader(new InputStreamReader((, null));
	    	
	    	//BufferedReader reader = new BufferedReader(new InputStreamReader(((HttpURLConnection) (new URL("")).openConnection()).getInputStream(),Charset.forName("UTF-8")));
	    	
	    	
	    	
	    	
	    	  // URL url1 = new URL("https://www.w3schools.com/css/img_5terre.jpg");

	    	  URL url1 = new URL("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Employee%20Declaration%20_Income%20tax_Form.xlsx");
	          HttpURLConnection con = (HttpURLConnection) url1.openConnection();
	 
	                // Setting the request method and
	                // properties.
	           
	 
	              
					/*
					 * InputStream ip = con.getInputStream();
					 * 
					 * BufferedReader br1 = new BufferedReader( new
					 * InputStreamReader(ip,Charset.forName("UTF-8")));
					 * 
					 * System.out.println(br1);
					 * 
					 * 
					 * // Printing the response code // and response message from server.
					 * System.out.println("Response Code:" + con.getResponseCode());
					 * System.out.println( "Response Message:" + con.getResponseMessage());
					 */
	    	
	    	    
	    	    //URLConnection urlConn = url.openConnection();
	    	    con.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
	    	    //urlConn.setReadTimeout(5000);
	    	    //urlConn.setConnectTimeout(5000);
	    	    con.setRequestProperty("Cookie", "foo=bar"); 

	    	    String contentType = con.getContentType();

	    	    System.out.println("contentType:" + contentType);

	    	    InputStream is = con.getInputStream();
	    	    
	    	    
	    	  //  BufferedReader reader = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
	    	
	    	   // File outputFile = new File("/home/husenaiah/Downloads/2022/pngtesttest20055.jpg");		
				File outputFile = new File("/home/husenaiah/Downloads/2022/pngtesttest20055execl.xlsx");
	    	    
	    	   // FileUtils.copyInputStreamToFile(is, outputFile);
	    	    
	    	    byte[] bytedata=is.readAllBytes();
	    	    System.out.println(bytedata.length);
	    	        // append = false
					/*
					 * FileOutputStream outputStream = new FileOutputStream(outputFile, false); int
					 * read; byte[] bytes = new byte[8192]; while ((read = is.read(bytes)) != -1) {
					 * outputStream.write(bytes, 0, read); }
					 * 
					 */
	    	    
	    	    
	    	   // byte[] bytedata=is.readAllBytes();

		
				  
				  try ( FileOutputStream outputStream1 = new FileOutputStream(outputFile); ) {
				  
				  outputStream1.write(bytedata); // Write the bytes and you're done.
				  
				  } catch (Exception e) { e.printStackTrace(); }
				 
	    	    
	    	 //   bis = new BufferedInputStream(is, 4 * 1024);
	    	   // bos = new BufferedOutputStream(new FileOutputStream(fileName.toString()));​
	    	//InputStream in = new URL("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Screenshot%20from%202022-06-05%2022-28-36.png").openStream();
	    	//Files.copy(in, Paths.get(FILE_NAME), StandardCopyOption.REPLACE_EXISTING);

	   
	    	
	    
	
			
			//InputStream stream = graphClient.me().messages("Message-ID").attachments("Attachment-ID").buildRequest().get();
				  
				
			
			
			
			 InputStream stream123 = graphClient.customRequest("/me/drive/items/cebd1b98-4e68-4808-8254-f13f95f67c50/content", InputStream.class)
		    			.buildRequest()
		    			.get();
		         System.out.println(stream);
			
			graphClient.sites().byId("kgmip.sharepoint.com,9cbf9fea-9611-4bd8-885e-43e2e5edab6b,c9a49ae3-fa6f-430b-82ed-fa748ad66c83").drive().root();
			
			System.out.println(graphClient);
			
			DriveCollectionPage drive = graphClient.me().drives().buildRequest().get();
			
					System.out.println(drive.getCurrentPage());
			SiteCollectionPage sites = graphClient.sites()
					.buildRequest()
					.get();
			
			Long  ss=sites.getCount();
			System.out.println(ss);
			
			System.out.println(sites.getCurrentPage());
			
			DriveRecentCollectionPage recent = graphClient.me().drive()
					.recent()
					.buildRequest()
					.get();
			System.out.println(recent);

	    
		//	InputStream stream = graphClient.customRequest("https://kgmip-my.sharepoint.com/personal/husenaiah_g_kgmip_onmicrosoft_com/Documents/Microsoft%20Teams%20Chat%20Files/Screenshot%20from%202022-06-05%2022-28-36.png", InputStream.class)
	    //			.buildRequest()
	    //			.get();
			
	
			//Drive children = graphClient.me().drive().buildRequest().get();
			//System.out.println(children);
		
			
			System.out.println(recent);
			
		
			
			DriveItemRequestBuilder dirb=graphClient.me().drive().items("cebd1b98-4e68-4808-8254-f13f95f67c50");
			
			System.out.println(dirb.createdByUser());
	    	
	    	System.out.println(stream.read());

		}
	 
		@Override
		protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
			HeroCard card = new HeroCard();
			card.setText("You can upload an image or select one of the following choices");
		
			// Note that some channels require different values to be used in order to get
			// buttons to display text.
			// In this code the emulator is accounted for with the 'title' parameter, but in
			// other channels you may
			// need to provide a value for other parameters like 'text' or 'displayText'.
			card.setButtons(new CardAction(ActionTypes.IM_BACK, "1. Inline Attachment", "1"),
					new CardAction(ActionTypes.IM_BACK, "2. Internet Attachment", "2"),
					new CardAction(ActionTypes.IM_BACK, "3. Uploaded Attachment", "3"));

			Activity reply = MessageFactory.attachment(card.toAttachment());
			return turnContext.sendActivity(reply).thenApply(resourceResponse -> null);

		}
	    
	protected CompletableFuture<Void> onMessageActivity12(TurnContext turnContext) {

		logger.info("getChannelData()=> " + turnContext.getActivity().getChannelData().toString());
		logger.info("getCallerId()=> " + turnContext.getActivity().getCallerId());
		logger.info("getSummary()=> " + turnContext.getActivity().getSummary());
		logger.info("getConversationId()=> " + turnContext.getActivity().getConversation().getId());
		logger.info("getConversationType()=> " + turnContext.getActivity().getConversation().getConversationType());
		logger.info("getConversationName()=> " + turnContext.getActivity().getConversation().getName());
		// turnContext.getActivity().getConversation().setIsGroup(true);
		logger.info("getFrom()=> " + turnContext.getActivity().getFrom().toString());
		logger.info("getChannelId()=> " + turnContext.getActivity().getChannelId());
		logger.info("getId()=> " + turnContext.getActivity().getId());
		logger.info("getReplyToId()=> " + turnContext.getActivity().getReplyToId());
		logger.info("getTopicName()=> " + turnContext.getActivity().getTopicName());
		logger.info("getText()=> " + turnContext.getActivity().getText());
		logger.info("getLabel()=> " + turnContext.getActivity().getLabel());
		logger.info("getAction()=> " + turnContext.getActivity().getAction());
		logger.info("getDeliveryMode()=> " + turnContext.getActivity().getDeliveryMode());
		logger.info("getImportance()=> " + turnContext.getActivity().getImportance());
		logger.info("getName()=> " + turnContext.getActivity().getName());
		logger.info("getText()=> " + turnContext.getActivity().getText());
		logger.info("getServiceUrl()=> " + turnContext.getActivity().getServiceUrl());
		logger.info("getType()=> " + turnContext.getActivity().getType());
		logger.info("getValueType()=> " + turnContext.getActivity().getValueType());
		logger.info("teamsGetTeamId()=> " + turnContext.getActivity().teamsGetTeamId());
		logger.info("getConversation().getName())=> " + turnContext.getActivity().getConversation().getName());
		logger.info("getRecipientId=> " + turnContext.getActivity().getRecipient().getId());
		logger.info("getFromId=> " + turnContext.getActivity().getFrom().getId());
		
		logger.info("getActivityId=> " + turnContext.getActivity().getId());
		logger.info("getActivityName=> " + turnContext.getActivity().getName());
		
		logger.info("getActivity().getConversation().getAadObjectId()=> " + turnContext.getActivity().getConversation().getAadObjectId());
		
		
	
		

		Attachment cardAttachment = null;
		if (turnContext.getActivity().getValue() != null) {
			LinkedHashMap botResponseMap = (LinkedHashMap) turnContext.getActivity().getValue();
			// String triggerClicked = (String) ((Map)
			// botResponseMap).get("ActionResponse");

			String stepExecution = null;

			if (((botResponseMap).get("Department")) != null) {
				cardAttachment = new Attachment();
				try {
					// supportService
					cardAttachment.setContent(Serialization.jsonToTree(supportService.createSupportAdaptiveCard(
							((String) (botResponseMap).get("Department")), botResponseMap, ticket, turnContext)));
					// cardAttachment.setContent(Serialization.jsonToTree(supportTyp));

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (((botResponseMap).get("SupportType")) != null) {
				cardAttachment = new Attachment();

				try {
					// cardAttachment.setContent(Serialization.jsonToTree(Ticket));
					cardAttachment.setContent(Serialization.jsonToTree(ticketService.createTicketAdaptiveCard(
							((String) (botResponseMap).get("SupportType")), botResponseMap, ticket, turnContext)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (((botResponseMap).get("IssueTitle")) != null) {
				cardAttachment = new Attachment();
				try {
					cardAttachment.setContent(
							Serialization.jsonToTree(ticketService.createTicket(botResponseMap, ticket, turnContext)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (((botResponseMap).get("Remarks")) != null) {
				cardAttachment = new Attachment();
				try {
					cardAttachment.setContent(Serialization.jsonToTree(ticketQualityService.ticketQualityRateUpdate(botResponseMap,ticket, turnContext)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			else {

				cardAttachment = new Attachment();
				commonUtility.removeContextData(ticket, turnContext);

				try {

					// cardAttachment.setContent(Serialization.jsonToTree(Department));
					cardAttachment.setContent(Serialization.jsonToTree(depService.createDepartmentAdaptiveCard()));

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {

			/*
			 * List<Department_23> departmentList=departmentImpl.findAll();
			 * System.out.println(departmentList.get(0));
			 */

			cardAttachment = new Attachment();
			commonUtility.removeContextData(ticket, turnContext);
			try {
				// cardAttachment.setContent(Serialization.jsonToTree(Department));
				cardAttachment.setContent(Serialization.jsonToTree(depService.createDepartmentAdaptiveCard()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		

		// String adaptiveCardString="{\"$schema\":
		// \"http://adaptivecards.io/schemas/adaptive-card.json\",
		// \"type\":\"AdaptiveCard\",\"version\": \"1.0\",\"body\": [{\"type\":
		// \"TextBlock\",\"text\": \"PublishAdaptiveCard schema\"}],\"actions\": []}";

		cardAttachment.setContentType("application/vnd.microsoft.card.adaptive");

		//16547 63426 661
		  Activity activity = MessageFactory.attachment(cardAttachment);
		 
		  
		  //activity.setReplyToId(activity.getId());
			/*
			 * Activity activity = Activity.clone(turnContext.getActivity());
			 * activity.setAttachment(cardAttachment);
			 * activity.setId(turnContext.getActivity().getReplyToId());
			 * 
			 * // activity.setReplyToId(turnContext.getActivity().getId());
			 * 
			 * logger.info("before retrun getReplyToId()=> " + activity.getReplyToId()); //
			 * activity.setId(turnContext.getActivity().getReplyToId()); //
			 * turnContext.updateActivity(activity);
			 * 
			 * logger.info(turnContext.getActivity().getChannelData().toString());
			 */
		  
			
				
				//ResourceResponse rp = turnContext.sendActivity(activity).join();
				 //activity.setId(rp.getId());
				
				
		  CompletableFuture<ResourceResponse> resourceresponse= turnContext.sendActivity(activity);
		  try {
			ResourceResponse rr= resourceresponse.get();
			ticketQualityService.updateCloseTicketMessageId(rr.getId(), ticket, turnContext);
		
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		//return turnContext.sendActivity(activity).th
		  return CompletableFuture.completedFuture(null);
		//return turnContext.sendActivity(activity).thenApply(sendResult -> null);

		// return turnContext.sendActivity(MessageFactory.text("Echo: " +
		// turnContext.getActivity().getText())).thenApply(sendResult -> null);

	}
	
     	
	   protected CompletableFuture<Void> onMessageReactionActivity(TurnContext turnContext) {
	        CompletableFuture<Void> task = null;

	        if (turnContext.getActivity().getReactionsAdded() != null) {
	            task = onReactionsAdded(turnContext.getActivity().getReactionsAdded(), turnContext);
	        }

	        if (turnContext.getActivity().getReactionsRemoved() != null) {
	            if (task != null) {
	                task.thenApply(
	                    result -> onReactionsRemoved(
	                        turnContext.getActivity().getReactionsRemoved(), turnContext
	                    )
	                );
	            } else {
	                task = onReactionsRemoved(
	                    turnContext.getActivity().getReactionsRemoved(), turnContext
	                );
	            }
	        }

	        return task == null ? CompletableFuture.completedFuture(null) : task;
	    }
	

		@Override
		protected CompletableFuture<InvokeResponse> onInvokeActivity(TurnContext turnContext) {
			
			
			logger.info("getChannelData()=> " + turnContext.getActivity().getChannelData().toString());
			logger.info("getCallerId()=> " + turnContext.getActivity().getCallerId());
			logger.info("getSummary()=> " + turnContext.getActivity().getSummary());
			logger.info("getConversationId()=> " + turnContext.getActivity().getConversation().getId());
			logger.info("getConversationType()=> " + turnContext.getActivity().getConversation().getConversationType());
			logger.info("getConversationName()=> " + turnContext.getActivity().getConversation().getName());
			// turnContext.getActivity().getConversation().setIsGroup(true);
			logger.info("getFrom()=> " + turnContext.getActivity().getFrom().toString());
			logger.info("getChannelId()=> " + turnContext.getActivity().getChannelId());
			logger.info("getId()=> " + turnContext.getActivity().getId());
			logger.info("getReplyToId()=> " + turnContext.getActivity().getReplyToId());
			logger.info("getTopicName()=> " + turnContext.getActivity().getTopicName());
			logger.info("getText()=> " + turnContext.getActivity().getText());
			logger.info("getLabel()=> " + turnContext.getActivity().getLabel());
			logger.info("getAction()=> " + turnContext.getActivity().getAction());
			logger.info("getDeliveryMode()=> " + turnContext.getActivity().getDeliveryMode());
			logger.info("getImportance()=> " + turnContext.getActivity().getImportance());
			logger.info("getName()=> " + turnContext.getActivity().getName());
			logger.info("getText()=> " + turnContext.getActivity().getText());
			logger.info("getServiceUrl()=> " + turnContext.getActivity().getServiceUrl());
			logger.info("getType()=> " + turnContext.getActivity().getType());
			logger.info("getValueType()=> " + turnContext.getActivity().getValueType());
			logger.info("teamsGetTeamId()=> " + turnContext.getActivity().teamsGetTeamId());
			logger.info("getConversation().getName())=> " + turnContext.getActivity().getConversation().getName());
			logger.info("getRecipientId=> " + turnContext.getActivity().getRecipient().getId());
			logger.info("getFromId=> " + turnContext.getActivity().getFrom().getId());
			
			logger.info("getActivityId=> " + turnContext.getActivity().getId());
			logger.info("getActivityName=> " + turnContext.getActivity().getName());
			
			logger.info("getActivity().getConversation().getAadObjectId()=> " + turnContext.getActivity().getConversation().getAadObjectId());

			Attachment cardAttachment = null;
			if (turnContext.getActivity().getValue() != null) {
				LinkedHashMap botResponseMap = (LinkedHashMap) turnContext.getActivity().getValue();
				LinkedHashMap actionObj = (LinkedHashMap) botResponseMap.get("action");
				String triggerClicked = (String) ((Map) actionObj).get("title");
				cardAttachment = new Attachment();
				try {
					cardAttachment.setContent(Serialization
							.jsonToTree(ticketQualityService.ticketStatusUpdate(triggerClicked, ticket, turnContext)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			cardAttachment.setContentType("application/vnd.microsoft.card.adaptive");
			Activity activity = MessageFactory.attachment(cardAttachment);
			// activity.setId(turnContext.getActivity().getReplyToId());
			// turnContext.updateActivity(activity);

			logger.info(turnContext.getActivity().getChannelData().toString());
			
		

			return turnContext.sendActivity(activity).thenApply(sendResult -> null);
			// String triggerClicked = (String) ((Map)
			// botResponseMap).get("ActionResponse");
			// return CompletableFuture.completedFuture(null);
		}
	 
	

	@Override
	protected CompletableFuture<MessagingExtensionResponse> onTeamsAppBasedLinkQuery(TurnContext turnContextt,
			AppBasedLinkQuery query) {

		ThumbnailCard card = new ThumbnailCard();
		card.setTitle("CodeProject");
		card.setText(query.getUrl());

		final String logoLink = "https://codeproject.freetls.fastly.net/App_Themes/CodeProject/Img/logo250x135.gif";
		CardImage cardImage = new CardImage(logoLink);
		card.setImages(Collections.singletonList(cardImage));

		// Create attachments
		MessagingExtensionAttachment attachments = new MessagingExtensionAttachment();
		attachments.setContentType(HeroCard.CONTENTTYPE);
		attachments.setContent(card);

		// Result
		MessagingExtensionResult result = new MessagingExtensionResult();
		result.setAttachmentLayout("list");
		result.setType("result");
		result.setAttachments(Collections.singletonList(attachments));

		// MessagingExtensionResponse
		return CompletableFuture.completedFuture(new MessagingExtensionResponse(result));

	}

	@Override
	protected CompletableFuture<Void> onMembersAdded(List<ChannelAccount> membersAdded, TurnContext turnContext) {

		System.out.println("user getAadObjectId => " + membersAdded.get(0).getAadObjectId());
		System.out.println("user getId => " + membersAdded.get(0).getId());

		return membersAdded.stream()
				.filter(member -> !StringUtils.equals(member.getId(), turnContext.getActivity().getRecipient().getId()))
				.map(channel -> turnContext.sendActivity(MessageFactory.text("Hello and welcome!")))
				.collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
	}
	
	private Attachment createAdaptiveCardAttachment()
		    throws URISyntaxException, IOException {     
		
		String filePath ="./src/main/resources/card.json";
		    try {
		        // Read JSON
		        InputStream inputStream =
		            this.getClass().getClassLoader().getResourceAsStream(filePath);
		        String adaptiveCardJson = IOUtils.toString(inputStream,
		            StandardCharsets.UTF_8);
		        // Replace placeholders with the actual values
		        adaptiveCardJson = StringUtils.replace(adaptiveCardJson,
		            "<USER_ID>", "Akash");
		        adaptiveCardJson = StringUtils.replace(adaptiveCardJson,
		            "<ID>", "Akash");
		        adaptiveCardJson = StringUtils.replace(adaptiveCardJson,
		            "<TITLE>", "Akash");
		        adaptiveCardJson = StringUtils.replace(adaptiveCardJson,
		            "<COMPLETED>", "Akash");
		        // Create attachment
		        Attachment attachment = new Attachment();
		        attachment.setContentType("application/vnd.microsoft.card.adaptive");
		        attachment.setContent(Serialization.jsonToTree(adaptiveCardJson));
		        return attachment;
		    }
		    catch(Exception e) {
		        e.printStackTrace();
		        return new Attachment();
		    }
		}
	
	
		@Override
		protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
			System.out.println("onConversationUpdateActivity");
			return CompletableFuture.completedFuture(null);

		}
		
	
	/*
	 * @Override public CompletableFuture<Void> onTurn(TurnContext turnContext) {
	 * System.out.println("onTurn"); return CompletableFuture.completedFuture(null);
	 * 
	 * }
	 * 
	
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onMembersRemoved(List<ChannelAccount> membersRemoved, TurnContext
	 * turnContext) { System.out.println("onMembersRemoved"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onMessageReactionActivity(TurnContext turnContext) {
	 * System.out.println("onMessageReactionActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onReactionsAdded(List<MessageReaction> messageReactions, TurnContext
	 * turnContext) { System.out.println("onReactionsAdded"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onReactionsRemoved(List<MessageReaction> messageReactions, TurnContext
	 * turnContext) { System.out.println("onReactionsRemoved"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void> onEventActivity(TurnContext
	 * turnContext) { System.out.println("onEventActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<InvokeResponse>
	 * onInvokeActivity(TurnContext turnContext) {
	 * System.out.println("onInvokeActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void> onSignInInvoke(TurnContext
	 * turnContext) { System.out.println("onSignInInvoke"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * protected static InvokeResponse createInvokeResponse(Object body) {
	 * System.out.println("createInvokeResponse"); return new
	 * InvokeResponse(HttpURLConnection.HTTP_OK, body); }
	 * 
	 * @Override protected CompletableFuture<Void> onTokenResponseEvent(TurnContext
	 * turnContext) { System.out.println("onTokenResponseEvent"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void> onEvent(TurnContext turnContext)
	 * { System.out.println("onEvent"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void> onInstallationUpdate(TurnContext
	 * turnContext) { System.out.println("onInstallationUpdate"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void> onCommandActivity(TurnContext
	 * turnContext) { System.out.println("onCommandActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onCommandResultActivity(TurnContext turnContext) {
	 * System.out.println("onCommandResultActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onInstallationUpdateAdd(TurnContext turnContext) {
	 * System.out.println("onInstallationUpdateAdd"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onInstallationUpdateRemove(TurnContext turnContext) {
	 * System.out.println("onInstallationUpdateRemove"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<AdaptiveCardInvokeResponse>
	 * onAdaptiveCardInvoke(TurnContext turnContext, AdaptiveCardInvokeValue
	 * invokeValue) { System.out.println("onAdaptiveCardInvoke"); return
	 * Async.completeExceptionally(new
	 * InvokeResponseException(HttpURLConnection.HTTP_NOT_IMPLEMENTED)); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onEndOfConversationActivity(TurnContext turnContext) {
	 * System.out.println("onEndOfConversationActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void> onTypingActivity(TurnContext
	 * turnContext) { System.out.println("onTypingActivity"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * @Override protected CompletableFuture<Void>
	 * onUnrecognizedActivityType(TurnContext turnContext) {
	 * System.out.println("onUnrecognizedActivityType"); return
	 * CompletableFuture.completedFuture(null); }
	 * 
	 * private AdaptiveCardInvokeValue getAdaptiveCardInvokeValue(Activity activity)
	 * throws InvokeResponseException {
	 * System.out.println("getAdaptiveCardInvokeValue"); if (activity.getValue() ==
	 * null) { AdaptiveCardInvokeResponse response =
	 * createAdaptiveCardInvokeErrorResponse( HttpURLConnection.HTTP_BAD_REQUEST,
	 * "BadRequest", "Missing value property"); throw new
	 * InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response); }
	 * 
	 * Object obj = activity.getValue(); JsonNode node = null; if (obj instanceof
	 * JsonNode) { node = (JsonNode) obj; } else { AdaptiveCardInvokeResponse
	 * response = createAdaptiveCardInvokeErrorResponse(
	 * HttpURLConnection.HTTP_BAD_REQUEST, "BadRequest",
	 * "Value property instanceof not properly formed"); throw new
	 * InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response); }
	 * 
	 * AdaptiveCardInvokeValue invokeValue = Serialization.treeToValue(node,
	 * AdaptiveCardInvokeValue.class); if (invokeValue == null) {
	 * AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
	 * HttpURLConnection.HTTP_BAD_REQUEST, "BadRequest",
	 * "Value property instanceof not properly formed"); throw new
	 * InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response); }
	 * 
	 * if (invokeValue.getAction() == null) { AdaptiveCardInvokeResponse response =
	 * createAdaptiveCardInvokeErrorResponse( HttpURLConnection.HTTP_BAD_REQUEST,
	 * "BadRequest", "Missing action property"); throw new
	 * InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response); }
	 * 
	 * if (!invokeValue.getAction().getType().equals("Action.Execute")) {
	 * AdaptiveCardInvokeResponse response = createAdaptiveCardInvokeErrorResponse(
	 * HttpURLConnection.HTTP_BAD_REQUEST, "NotSupported",
	 * String.format("The action '%s'is not supported.",
	 * invokeValue.getAction().getType())); throw new
	 * InvokeResponseException(HttpURLConnection.HTTP_BAD_REQUEST, response); }
	 * 
	 * return invokeValue; }
	 * 
	 * private AdaptiveCardInvokeResponse
	 * createAdaptiveCardInvokeErrorResponse(Integer statusCode, String code, String
	 * message) { System.out.println("getAdaptiveCardInvokeValue");
	 * AdaptiveCardInvokeResponse adaptiveCardInvokeResponse = new
	 * AdaptiveCardInvokeResponse();
	 * adaptiveCardInvokeResponse.setStatusCode(statusCode);
	 * adaptiveCardInvokeResponse.setType("application/vnd.getmicrosoft().error");
	 * com.microsoft.bot.schema.Error error = new com.microsoft.bot.schema.Error();
	 * error.setCode(code); error.setMessage(message);
	 * adaptiveCardInvokeResponse.setValue(error); return
	 * adaptiveCardInvokeResponse; }
	 */
    
}