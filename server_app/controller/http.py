from app import main, app
from model import *

import sqlalchemy
from flask import request, render_template

from datetime import datetime, timedelta
import functools
import json

def authenticated_only_http(f):
    @functools.wraps(f)
    def wrapped(*args, **kwargs):
        data = json.loads(request.data)
        user_id = data.get("user_id")
        session = data.get("session")
        
        if user_id is None or session is None:
            print "Failed auth: Did not supply credentials"
            return
        elif not User.session_in_user(user_id, session, UserSession):
            print "Failed auth: Not in session"
            return
        return f(*args, **kwargs)
    
    return wrapped

def render_template_custom(path,**kwargs):
    return render_template(path, session=request.cookies.get("session"), **kwargs)

@main.route('/', methods=['GET'])
def welcome():
    print request.cookies
    return render_template_custom('welcome.html')
        
## The password is checked here
@main.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'GET':
        return render_template_custom('login.html')
    elif request.method == 'POST':
        auth = json.loads(request.data)

        if auth is not None:
            username, password = auth['username'], auth['password']
            user = User.query.filter_by(username=username).first()
            
            if user is not None:
                session = user.authenticate(password, UserSession)
                if session != None:
                    print "%r has logged in." % user
                    return json.dumps({"authenticated": True, "user_id": user.id, "session": session})
                return json.dumps({"authenticated": False, "reason": "Either session is invalid or session has expired."})
            return json.dumps({"authenticated": False, "reason": "Username does not exist."})
        return json.dumps({"authenticated": False, "reason": "Failed to collect credentials."})
    
@main.route('/signup', methods=['GET', 'POST'])
def signup():
    if request.method == 'GET':
        return render_template_custom('signup.html')
    elif request.method == 'POST':
        cred = json.loads(request.data)
        
        if cred is not None:
            username, password = cred['username'], cred['password']
            user = User(username)
            session = user.register(password, UserSession)
            if session != None:
                print "%r has signed up." % user
                return json.dumps({"registered": True, "user_id": user.id, "session": session})
        return json.dumps({"registered": False})

@main.route('/fetchrooms', methods=['POST'])
@authenticated_only_http
def fetch_rooms():
    global Room
    global User
    
    rooms = []
    for room in Room.query.all():
        created_by_user = User.query.get(room.created_by_user_id)
        if created_by_user is None:
            created_by_user = {"id": -1, "username": "deleted"}
        rooms.append({
            "room_id": room.id,
            "room_name": room.room_name,
            "user_id": created_by_user.id,
            "username": created_by_user.username,
            "date": str(room.created_date)
        })

    return json.dumps({"rooms": rooms})

@main.route('/createroom', methods=['POST'])
@authenticated_only_http
def create_room():
    data = json.loads(request.data)
    
    if data is not None:
        global Room, User
        print "%r is creating room '%r'" % (data.get("username"), data.get("room_name"))
        try:
            room = Room(data.get('room_name'), data.get("user_id"))
            room.commit()
        except sqlalchemy.exc.IntegrityError as e:
            # code, msg = e.orig
            c = {"created": False}
            return c
        return json.dumps({"created": True, "room_id": room.id})
    return json.dumps({"created": False})

@main.route('/user/<other_user_id>', methods=['POST'])
@authenticated_only_http
def query_user(other_user_id):
    global User, Friend
    user = User.query.get(other_user_id)
    
    friend = Friend.query.filter(sqlalchemy.or_(Friend.req_user_id == other_user_id, Friend.res_user_id == other_user_id)).first()
    
    if user is not None:
        outputDict = {
                "username": user.username, 
                "created_date": str(user.created_date), 
                "last_active_date": str(user.last_active_date), 
                "online": datetime.utcnow() - user.last_active_date < timedelta(minutes=15)
        }
        
        outputDict["is_friend"] = friend is not None and not friend.request
        outputDict["has_requested_to_be_friends"] = friend is not None and friend.request and friend.req_user_id == int(other_user_id)
        outputDict["has_sent_friend_request"] = friend is not None and friend.request and friend.res_user_id == int(other_user_id)
    
        return json.dumps(outputDict)
    return json.dumps({"error": "User ID %r does not exist." % other_user_id})

@main.route('/friend', methods=['POST'])
@authenticated_only_http
def friend():
    global User, Friend
    data = json.loads(request.data)
    # If they are not the same user
    user_id = data.get("user_id")
    friend_user_id = data.get("friend_user_id")
    if user_id != friend_user_id:
        friend = Friend.query.filter(sqlalchemy.or_(Friend.req_user_id == user_id, Friend.res_user_id == user_id)).first()
        if friend is not None:
            if friend.request:
                if friend.req_user_id == user_id:
                    # Remove friend request
                    print "Removing friend request..."
                    friend.remove()
                    return json.dumps({"success": True})
                elif friend.res_user_id == user_id:
                    # Accept friend request
                    print "Accepting friend request..."
                    friend.request = False
                    friend.update()
                    return json.dumps({"success": True})
            else:
                # Remove friend
                print "Removing friend..."
                friend.remove()
                return json.dumps({"success": True})
        else:
            print "Adding friend request..."
            friend = Friend(user_id, friend_user_id)
            friend.addAndCommit()
            return json.dumps({"success": True})
    return json.dumps({"error": "Same User ID %r when trying to send friend request, accept friend request, or remove friends." % user_id})

@main.route('/logout', methods=['POST'])
@authenticated_only_http
def logout():
    cred = json.loads(request.data)    
    if cred is not None:
        username, user_id, session = cred.get('username'), cred.get('user_id'), cred.get('session')
        if username is not None and user_id is not None and session is not None:
            user = User.query.get(user_id)
            if user is not None:
                print "%s has logged out." % user
                user.remove_session(session, UserSession)
                return json.dumps({"logged out": True})
    return json.dumps({"logged out": False})

@app.errorhandler(404)
def page_not_found(e):
    return render_template_custom('404.html'), 404
