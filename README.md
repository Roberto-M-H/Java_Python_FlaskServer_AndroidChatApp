# Chat
A simple chat application for android with a python flask server as the backend.

## Setup
### Chat server: Ubuntu
Run this command to clone the repository, install dependencies, and get the Chat server up and running.
```
curl https://raw.githubusercontent.com/jos0003/Chat/master/setup_server.sh | sh
```

### Chat server: Other operating systems
* Clone the repository
```
git clone https://github.com/jos0003/Chat
```
* Install these dependencies:
  * python
  * pip packages
    * flask
    * flask-bcrypt
    * flask-socketio
    * sqlalchemy
    * flask-sqlalchemy
  * bcrypt (flask-bcrypt needs this)
  * sqlite for portable database (easier to setup than mysql)
* Run `python /path/to/Chat/server_app` to start the server.

### Chat client: Web
(Currently work in progress)

Once Chat server is running, Chat client is accessible via a web browser by heading to `http://localhost:<port>`

### Chat client: Android 
* Open `android_app` gradle project in either Android Studio or Eclipse with the gradle plugin.
* Install android application on your phone and run the app.

## Run Chat server
* Run `python /path/to/Chat/server_app` to start the server.
 * Use `screen` or `tmux` if you want to run the server without staying logged in to a terminal.

## Features
* Able to remember user when logging in/signing up
* Rooms
  * Public rooms (displays a list of rooms to join, and a edittext and button to host a room)
  * Display rooms in a list
* User
  * User profile (username, joined date, last active date, is online)
* Friends
  * Ability to add (via requests) and remove friends
  * Display friends in a list

## TODO:

### At all stages of development
* Refactor code
* Remove bugs
* Refine UI constantly

### Friends
* Show friend requests within the same listview
* Private message chat to a single friend
* Block list
* Ordering
* Able to search a friend

### Delete access
* Ability to delete room
* Ability to delete account

### Caching 
* Messages
* Rooms
* Friends

### Communication
* Replace HTTP POST API requests with RESTful HTTP API requests
* Isolate controller code from activities within android application
* Ensure message delivery

### Rooms
* Private rooms (request to join, join with password)
* Able to search a room
* Vote rooms
* Ordering (most recent, most voted)

### Chat
* Show where people have read up to (like in google+ hangouts)
* Notifications
* End to end encryption (via HTTPS + Let's Encrypt; will sign public keys if requested)
* Able to search chat messages

### Web
* Replicate android features onto the web application

### User
* Able to edit and save user bio
* Able to recover by using email address (optional)
* Show user activity in user profile

### Welcome screen
* Dynamically form-validate welcomeActivity as credentials are being entered.

### Server
* Use daemon instead of using `screen` or `tmux` to run the server.

### Testing
* Write test cases
