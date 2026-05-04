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

👉 In a typical Spring Boot app using JWT (JSON Web Tokens), you are working in a Stateless Architecture. This means the server is like a shopkeeper with amnesia—it doesn't remember who you are once you walk out the door; it only trusts you because you are carrying a valid, signed "Receipt" (the Token). This creates a specific problem when it comes to logging out.

## 🛑 The Logout Problem: The "Unstoppable" Token
In a Stateful app (using Sessions), logging out is easy: the server deletes the session ID from its memory, and the user is instantly kicked out. \
In a Stateless app, the server has no memory of the token it gave you. \
• The token is self-contained. \
• As long as the token hasn't expired and the signature is valid, the server must accept it. \
• The Problem: If a user clicks "Logout," but an attacker steals their token 5 minutes earlier, that attacker can still use the token until it expires. The server has no way to "cancel" it.


# 🛠️ The Three Common Solutions
Since we can't "delete" a token from the user's hand, we have to use these strategies:

1. **The "Clean Your Own Room" Method (Client-Side)** \
	* How it works: When the user clicks logout, the frontend (React/Angular/Mobile) simply deletes the token from its local storage. \
	* The Flaw: This is "fake" security. The token still exists and is valid in the "wild." If someone else has a copy of it, they can still use it. \

2. **The Blacklist Method (The "Banned List")** \
	* How it works: You create a fast database (like Redis). When a user logs out, you take their token and put it on a "Blacklist" until its original expiry time. \
	* The Check: Every time a request comes in, the server checks: "Is this token valid? YES. Is it on the Blacklist? NO." \
	* The Trade-off: You are now slightly "Stateful" again because the server has to remember which tokens are bad. \

3. **Short Lifespans + Refresh Tokens** \
	* How it works: You give the user an Access Token that only lasts 5 or 15 minutes. You also give them a Refresh Token that lasts days. \
	* Logout: When the user logs out, you delete the Refresh Token from your database. \
	* Result: The user might still be able to use the Access Token for a maximum of 5–15 minutes, but they can never get a new one.
	* Yes, we are consuming space. But here is why it’s different (and better) than traditional session management:

    _3.1. Where do we store it?_ \
	We don't usually store these in our main, heavy SQL database (like MySQL or PostgreSQL). We store them in a Fast In-Memory Store like Redis. Why? Redis is incredibly fast and allows us to set a TTL (Time To Live). \
	Automatic Cleanup: When a token is set to expire in 7 days, Redis automatically deletes it from memory the second that time is up. We don't have to write "cleanup" scripts. \

    _3.2. What exactly are we storing?_ \
	We aren't usually storing the massive, long JWT string itself. We often store a Token ID (jti) or a User ID mapped to a version number. \
	Traditional Session: Stores the entire user profile, permissions, and metadata in the server's RAM for every single active user. \
	Refresh Token Storage: Only stores a tiny "Reference Key" (a few bytes). The "ID Badge" (Access Token) is still carried by the user. \

	_3.3. Why it’s still considered "Stateless-ish"_ \
	Even though we are storing something, the architecture remains "Stateless" because: \
	The Access Token (the one used for every API call) is never stored. The server validates it mathematically using a Secret Key. \
	The Database is only hit once in a while. **`You only check the database when the 15-minute Access Token expires and the user needs a new one. For the other 99% of requests, the server doesn't look at the database at all`**.

## Understanding three layers: the SecurityContextHolder, the SecurityContext, and the Authentication object
---
1. SecurityContextHolder (The Storage Location) \
`What it is:` The "Global Filing Cabinet." It is the top-level object where Spring Security stores all security details. \
`How it works:` It uses a ThreadLocal strategy. This means it creates a "private pocket" for every single web request. \
`Why it exists:` So you can access the current user's info from anywhere in your code (Service, Controller, etc.) without having to pass it as a method parameter.\
`Mental Image:` A wall of lockers where each worker (Thread) has their own private locker that only they can open.

2. SecurityContext (The Folder) \
`What it is:` The "Specific Folder" inside the locker. It is a simple container that lives inside the SecurityContextHolder. \
`Its only job:` To hold the Authentication object. \
`Mental Image:` A physical folder. It doesn't "do" much; it just keeps the ID papers organized and easy to grab.

5. Authentication  \
`What it is:` The actual "ID Badge" sitting inside the folder. This is the most important part because it contains the actual user data. \
`Mental Image:` A high-security ID card with your photo, name, and a list of doors you are allowed to unlock. \
• Principal: Who you are (usually a UserDetails object). \
• Credentials: Your password (usually deleted after you're logged in for safety). \
• Authorities: Your permissions (e.g., ROLE_ADMIN).

### 🚀 The Logic Flow (In One Sentence)
To find out who is logged in, you go to the Holder (the locker), grab the Context (the folder), and look at the Authentication (the ID badge).

The "Magic" Code Line
```Java
// "Go to the locker, open the folder, and show me the ID badge"
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

### 3 Rules to Remember
**Isolation:** Thread A can never see Thread B's security info. This prevents User A from accidentally acting as User B. \
**Statelessness:** In most web apps, this "locker" is emptied and deleted as soon as the web request is finished (the response is sent). \
**The Static Rule:** There is only one SecurityContextHolder class, but it manages millions of SecurityContexts (one for every user). \

---
## Understanding Stateful and Stateless

### Stateful Architecture 
In a stateful system, the server keeps track of the "state" (the context or memory) of each user's connection. \
• **How it works:** When you log in, the server creates a file in its memory that says, "Session #12345 belongs to Rishabh." It then gives your browser a tiny cookie that just says #12345.  \
• **The next click:** When you click "View Profile," your browser hands the server the #12345 cookie. The server looks in its filing cabinet, finds #12345, sees it belongs to you, and loads your profile. \
• **Pros:** It's incredibly easy to manage. If you want to log out, the server just throws away the #12345 file. Boom, you are logged out. \
• **Cons:** It takes up server memory. If you have a million users logged in at once, your server needs a massive filing cabinet to remember all those sessions. 

### Stateless Architecture 
In a stateless system, the server retains zero memory of past requests. Every single HTTP request you send must contain all the information necessary for the server to understand and process it. \
• **How it works:** This is where JWTs live! When you log in, the server doesn't save anything in its own memory. Instead, it creates a JWT (containing your name, ID, and role), mathematically signs it, hands it to you, and immediately forgets you exist. \
• **The next click:** When you click "View Profile," you must hand the server the entire JWT. The server looks at it, does the math to verify the signature is real, serves your profile, and forgets you again. \
• **Pros:** It is incredibly scalable. You can have 10 million users, and the server uses zero extra memory because the users are carrying their own ID badges (the tokens). It's also great for microservices—any server can verify the math without needing access to a central session database. \
• **Cons:** The "Logout Problem." Because the server doesn't have a filing cabinet to cross your name out of, it can't easily invalidate a token before its expiration time runs out. 



