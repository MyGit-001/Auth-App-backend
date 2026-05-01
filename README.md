# 👉 I used to hate Git. Today, it saved my project.

I’ll admit it, I’ve always found Git confusing. I only committed my code "for namesake" because I thought I had to. That changed today. \
I ran into a nightmare scenario 😱 --> A Spring Boot starter parent version downgrade (4.0.3 to 3.2.5) triggered a waterfall of annotation errors.  \
IntelliJ stopped recognizing my App.java as the root. I spent hours clearing caches, deleting .idea folders, and tweaking Maven settings based on Gemini AI suggestions. Nothing worked by the way it was gemini who told me to change the version 😑

Frustrated and ready to give up both myself 😣 and Gemini 🤖, I realized now I just wanted to go back to the prev point of my project which actually worked. 

The lesson: I finally looked into how to actually "revert" using Git. I discovered that what I thought was a complex tool is actually a massive safety net. For the first time, Git wasn't a chore—it was the "undo" button I desperately needed. \
📢 _If you’re avoiding Git because it feels like extra work, take it from me: You’re not just managing code --> you’re buying insurance for your sanity._


🔑 What i learned:
	

1. `git log --oneline` (The History Map)
Before you fix anything, you need to see where you are.
	* What it does: Shows a simple list of every "Save Point" (commit) you’ve ever made.
	* The "Hash": Each commit has a unique 7-character code (e.g., a1b2c3d). You will need this code to tell Git exactly which version you want to go back to.
3. `git revert <commit hash>` (The "Correction" Sticker)
	* The Analogy: Imagine you wrote a bad sentence in a notebook. Instead of erasing it, you write a new line below it saying, "Ignore the line above; it was a mistake."
	* Best for: When you’ve already shared your code with a team. It doesn't "delete" the past; it just adds a new "fix" commit on top. It is the safest way to undo.
3. `git reset --hard <commit hash>` (The "Time Travel" Button)
	* The Analogy: You literally travel back in time to a specific day. Everything you did after that day is deleted from existence.
	* Warning: This is powerful but "dangerous." It wipes away any unsaved work and deletes newer commits.
	* Best for: When you are working alone and you've made such a mess that you just want to "delete" the last hour (or day) of work and start fresh from a known good state.
4. `git clean -fd` (The "Trash Collector")
	* -f = force; -d = remove untracked directories 
	* What it does: Removes untracked files and untracked directories only. It will not remove changes inside a file that Git already tracks. To discard changes inside a tracked file, use a reset/restore command instead. 
	* Best use: when you want to remove the untracked files themselves.
	* The Analogy: Resetting or Reverting handles the files Git knows about. But what about those random folders and new files you created that Git hasn't "tracked" yet?
	* What it does: It hunts down any files or folders that aren't part of your Git history and deletes them.
	* Best for: Cleaning up "junk" like IDE configuration files (.idea), build folders, or random test files you created while debugging.

## ✅ When to use which
* Use **git revert** when: the bad commit is already pushed and other people may have based work on it; you want a safe, auditable undo. 
* Use **git reset --hard** when: you are certain you want to discard local commits and uncommitted changes, and you either are the only one working on the branch or you coordinate with teammates before force-pushing.


# Let's see Global Exception Handling for REST APIs 
The Step-by-Step Flow \
Here is what happens when you request a user with an email that does not exist in the database. 
### Step 1: The API Call is Made 
You go into Postman and make a GET request to an endpoint like: GET /api/v1/users/emailId/user-does-not-exist@example.com 
### Step 2: The UserController Receives the Request 
Spring sees the URL and directs the request to the correct method in your UserController.

```Java
// UserController.java

@GetMapping("/emailId/{emailId}")
public ResponseEntity<UserDto> getUserByEmail(@PathVariable("emailId") String emailId) {
    // The controller's only job is to delegate. It calls the service.
    // It has no idea if the user exists or not.
    return ResponseEntity.ok(userService.getUserByEmail(emailId));
}
```

The controller immediately calls the getUserByEmail method in your UserServiceImpl, passing along the email address. 
### Step 3: The UserServiceImpl Does the Work (and Finds a Problem) 
Now we are in the "workshop". The service tries to find the user.

```Java
// UserServiceImpl.java

@Override
public UserDto getUserByEmail(String email) {
    // 1. The service asks the repository to find a user by email.
    //    The database finds nothing, so the repository returns an empty Optional.
    User user = userRepo.findByEmail(email)
        
        // 2. The .orElseThrow() method is now triggered because the Optional is empty.
        .orElseThrow(() -> new ResourceNotFoundException("User not found with Email ID")); // <-- THE EXCEPTION IS THROWN!

    // 3. IMPORTANT: The code below this line is NEVER executed.
    //    The method stops immediately and throws the exception "upstairs".
    return modelMapper.map(user, UserDto.class);
}
```

This is the most critical moment. The throw keyword acts like a fire alarm. It immediately stops the normal flow of the getUserByEmail method. The method does not return a UserDto. Instead, it "throws" the ResourceNotFoundException object up to whatever called it (which was the UserController).
### Step 4: The Exception Reaches the GlobalExceptionHandler 
The ResourceNotFoundException object now "bubbles up". The UserController doesn't know what to do with it (it has no try-catch block), so the exception continues traveling upwards. \
This is where your Emergency Manager steps in. \
The @RestControllerAdvice annotation on your GlobalExceptionHandler tells Spring: "I am a special component that watches over all controllers. If any unhandled exception bubbles up, let me see it first!" \
Spring shows the ResourceNotFoundException to your GlobalExceptionHandler. The handler looks at its methods and finds a perfect match: 

```Java
// GlobalExceptionHandler.java


@RestControllerAdvice
public class GlobalExceptionHandler {

    // This annotation says: "I am the method that handles ResourceNotFoundException!"
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        // ... The code to handle the error goes here ...
    }
}
```

Because it found a matching handler, the application does not crash. Your handler takes control of the entire process. 
### Step 5: The Handler Crafts a Beautiful Response 
Your handler method now executes. The exception object it receives as a parameter is the exact same one you created in your service. 

```Java
// GlobalExceptionHandler.java

@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
    
    // 1. Get the message from the exception you threw earlier.
    String message = exception.getMessage(); // "User not found with Email ID"

    // 2. Create a clean, structured ErrorResponse object.
    ErrorResponse errorResponse = new ErrorResponse(message, 404, "Resource Not Found");

    // 3. Build a full ResponseEntity package, setting the HTTP status to 404
    //    and putting your ErrorResponse object in the body.
    return ResponseEntity.status(404).body(errorResponse);
}
```
Our ErrorResponse is a record in out dTos which looks like this,
```Java
public record ErrorResponse(
        String message,
        HttpStatus status,
        String error) {
}
```

### Step 6: The Final Response is Sent to the Client 
Spring takes the ResponseEntity your handler created, converts the ErrorResponse object into a JSON string, and sends it back to Postman. \
What you see in Postman is: 
• Status: 404 Not Found
• Body:  JSON  
```JSON
	{
        "message": "User not found with Email ID",
        "statusCode": 404,
        "details": "Resource Not Found"
    }
```

<img width="897" height="382" alt="image" src="https://github.com/user-attachments/assets/87734795-0c0b-46aa-ba93-a18380832b9b" />

## Similarily, handling the exceptions for the createUser class where throwing IllegalArgumentException 
If no email was inputtted by the user --> "Email is required" \
If email already exists in the database --> "Email already exists"

```java 
public UserDto createUser(UserDto userDto){
        if(userDto.getEmail()==null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if(userRepo.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        User user = modelMapper.map(userDto , User.class);
        user.setProvider(userDto.getProvider()!=null ? userDto.getProvider() : Provider.LOCAL);
        User savedUser = userRepo.save(user);
        return modelMapper.map(savedUser , UserDto.class);
    }
```
Now defining the handler for IllegalArgumentException in Global handler, We have already defined the ErrorResponse record, so just passing the argument to it and returning it with ResponseEntity)

```Java
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage() , HttpStatus.BAD_REQUEST
                , "Resource Not Found" );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
```
API reposne if user was attepmpted to created with an already existing email Id. 
<img width="925" height="530" alt="image" src="https://github.com/user-attachments/assets/c27a30db-eeea-4df9-a6b8-9f9004e2927c" />

API reposne if user was attepmpted to be created without an email Id. 
<img width="907" height="550" alt="image" src="https://github.com/user-attachments/assets/e08c7fc7-c882-4847-bb11-ee357549e52c" />



## Understanding JWT (JSON Web Token) Authentication
---
1. Why use JWT instead of Basic Authentication? \
Basic authentication is less secure for public-facing clients (like React/Angular apps) because it requires sending the raw username and password with every single request. \
JWT authentication provides a more secure, stateless way to authenticate API requests.

2. What is a JWT? \
A JWT is an encoded string that securely transmits information between parties. It consists of three parts separated by dots (.):\
**`Header.Payload.Signature`**

Below is the Raw Token (What the Client Sees)
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlJpc2hhYmgiLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3MTUxMjM0NTZ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```
• Header: Contains metadata like the type of token (JWT) and the encryption algorithm used. \
`eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9`
```Json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
• Payload (Claims): Contains the actual data/information you want to store (e.g., User ID, roles, issue date). Note: Do not put sensitive data like passwords here, as it can be decoded by anyone. \
`eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlJpc2hhYmgiLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3MTUxMjM0NTZ9`
```Json
{
  "sub": "1234567890",
  "name": "Rishabh",
  "role": "ADMIN",
  "iat": 1715123456
}
```
• Signature: The most critical part. It is created using the encoded header, encoded payload, and a secret key. It ensures the token has not been tampered with and verifies the sender's identity. \
`SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`

If a hacker intercepts the token and tries to change their "role": "USER" to "role": "ADMIN", the payload's Base64 string changes. When the backend receives this altered token, it recalculates the signature.  \
Because the payload changed (and the hacker doesn't know the Secret Key to generate a new valid signature), the recalculated signature won't match the signature on the token. The backend immediately rejects it as tampered! 

3. The JWT Workflow (Client & Backend Interaction)
   
### Scenario A: Generating the Token (Login)
The client (e.g., React app) sends the username and password to a dedicated login API endpoint. \
The backend validates the credentials using the `AuthenticationManager -> AuthenticationProvider -> UserDetailsService (checking against the database)`. \
If valid, a `JwtService` generates a JWT token containing the user's information. \
The backend sends this token back to the client as a response. \
The client stores the token (e.g., in Local Storage).

### Scenario B: Accessing Protected APIs
The client sends a request to a protected API and includes the JWT token (usually in the Authorization header). \
A custom `JWT Authentication Filter` on the backend intercepts the request and extracts the token. \
The filter verifies the token's signature. \
If verified, the filter creates an Authentication object and sets it in the `SecurityContextHolder`. \
The Spring Security framework sees that the context is authenticated and allows the request to proceed to the protected API. 

### Scenario C: Access Denied
If the client tries to access a protected API without a token, the custom filter cannot verify them. \
The SecurityContextHolder remains unauthenticated (empty), and Spring Security blocks the request, returning an unauthorized error. 
