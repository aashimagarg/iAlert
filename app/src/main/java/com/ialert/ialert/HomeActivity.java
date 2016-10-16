package com.ialert.ialert;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int NUM_POSTS = 10;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private static final String DATA = "data";
    private ListView lvFriends;
    private ArrayList<Friend> topFriends = new ArrayList<>();
    FriendsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FacebookSdk.sdkInitialize(getApplicationContext());
        lvFriends = (ListView) findViewById(R.id.lvFriends);
        itemsAdapter = new FriendsAdapter(this, topFriends);
        lvFriends.setAdapter(itemsAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        /*
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };*/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "User after LogOut: " + FirebaseAuth.getInstance().getCurrentUser());
                //log out of firebase and fb and go to login activity
                FirebaseAuth.getInstance().signOut();
                //used solely to reset the automated facebook login button
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        facebookOneCall2();
        //facebookCalls();
    }

    void facebookOneCall() {
        final Map<String, Integer> friendsToCount = new HashMap<>();

        // grab posts
        GraphRequest requestIds = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/posts?fields=likes,comments",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                        if (jsonArray.length() != 0) {
                            // likes
                            for (int i = 0; i < NUM_POSTS; i++) {
                                if (jsonArray.optJSONObject(i).optJSONObject("likes") != null){
                                    JSONArray likesArray = jsonArray.optJSONObject(i).optJSONObject("likes").optJSONArray("data");
                                    for (int j = 0; j < likesArray.length(); j++) {
                                        String id = likesArray.optJSONObject(j).optString("id");
                                        if (!friendsToCount.containsKey(id)) {
                                            // create new friend
                                            friendsToCount.put(id, 1);
                                        } else {
                                            // increment count
                                            friendsToCount.put(id, friendsToCount.get(id) + 1);
                                        }
                                    }
                                }
                                if (jsonArray.optJSONObject(i).optJSONObject("comments") != null) {
                                    JSONArray commentsArray = jsonArray.optJSONObject(i).optJSONObject("comments")
                                            .optJSONArray("data");
                                    for (int j = 0; j < commentsArray.length(); j++) {
                                        String id2 = commentsArray.optJSONObject(j).optJSONObject("from").optString("id");
                                        if (!friendsToCount.containsKey(id2)) {
                                            // create new friend
                                            friendsToCount.put(id2, 1);
                                        } else {
                                            // increment count
                                            friendsToCount.put(id2, friendsToCount.get(id2) + 1);
                                        }
                                    }
                                }
                            }
                            //store friends
                            DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("users/" +
                                    FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "/friends");
                            dataRef.setValue(friendsToCount);
                        } else {
                            Toast.makeText(getApplicationContext(), "hi" + getString(R.string.no_posts),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        requestIds.executeAsync();
    }

    void facebookOneCall2() {
        final Map<String, List> friendsToCount = new HashMap<>();
        // grab posts
        GraphRequest requestIds = new GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "me/posts?fields=likes,comments",
            null,
            HttpMethod.GET,
            new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                    if (jsonArray.length() != 0) {
                        // Likes per user
                        for (int i = 0; i < NUM_POSTS; i++) {
                            // Increment counter for each user's likes
                            if (jsonArray.optJSONObject(i).optJSONObject("likes") != null){
                                JSONArray likesArray = jsonArray.optJSONObject(i).optJSONObject("likes")
                                        .optJSONArray("data");
                                for (int j = 0; j < likesArray.length(); j++) {
                                    String id = likesArray.optJSONObject(j).optString("id");
                                    String name = likesArray.optJSONObject(j).optString("name");
                                    if (!friendsToCount.containsKey(id)) {
                                        // create new friend in Map
                                        friendsToCount.put(id, Arrays.asList(name, 1));
                                    } else {
                                        // increment counter for an existing friend in Map
                                        friendsToCount.put(id, Arrays.asList(name,
                                                Integer.parseInt(friendsToCount.get(id).get(1).toString()) + 1));
                                    }
                                }
                            }
                            // Increment counter for each user's comments
                            if (jsonArray.optJSONObject(i).optJSONObject("comments") != null) {
                                JSONArray commentsArray = jsonArray.optJSONObject(i).optJSONObject("comments")
                                        .optJSONArray("data");
                                for (int j = 0; j < commentsArray.length(); j++) {
                                    String id2 = commentsArray.optJSONObject(j).optJSONObject("from").optString("id");
                                    String name2 = commentsArray.optJSONObject(j).optJSONObject("from").optString("name");
                                    if (!friendsToCount.containsKey(id2)) {
                                        // create new friend in Map
                                        friendsToCount.put(id2, Arrays.asList(name2, 1));
                                    } else {
                                        // increment counter for an existing friend in Map
                                        friendsToCount.put(id2, Arrays.asList(name2,
                                                Integer.parseInt(friendsToCount.get(id2).get(1).toString()) + 1));
                                    }
                                }
                            }
                        }

                        // Compute the top five friends of the user in increasing order (1-5)
                        int prevMax = Integer.MAX_VALUE;
                        String userName = "";
                        for (int x = 0; x < 5; x++) {
                            // Keep track of maximum value and user information
                            int max = 0;
                            String userId = "";
                            // Iterate through all the friends who commented and liked posts
                            for (int y = 0; y < friendsToCount.size(); y++) {
                                Object[] keys = friendsToCount.keySet().toArray();
                                int checkVal = Integer.parseInt(friendsToCount.get(keys[y].toString()).get(1).toString());
                                String checkName = friendsToCount.get(keys[y].toString()).get(0).toString();
                                boolean flag = true;
                                for (int index = 0; index < topFriends.size(); index++) {
                                    // check if topFriends contains the name already
                                    if (topFriends.get(index).getName().equals(checkName)) {
                                        flag = false;
                                    }
                                }
                                // Find topFriends in decreasing order for each iteration of the for loop
                                if (checkVal > max && checkVal <= prevMax && flag) {
                                    max = checkVal;
                                    userName = checkName;
                                    userId = keys[y].toString();
                                }
                            }
                            prevMax = max;
                            topFriends.add(new Friend(userId, userName));
                        }
                        itemsAdapter.notifyDataSetChanged();
                        // Store friends in Firebase
                        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("users/" +
                                FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "/friends");
                        dataRef.setValue(friendsToCount);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_posts), Toast.LENGTH_LONG).show();
                    }
                }
            });
        requestIds.executeAsync();
    }

    void facebookCalls() {
        // network calls
        final ArrayList<String> postIds = new ArrayList<String>();
        final Map<String, Integer> friendsToCount = new HashMap<>();

        // grab posts
        GraphRequest requestIds = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/posts",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                        if (jsonArray.length() != 0) {
                            for (int i = 0; i < NUM_POSTS; i++) {
                                postIds.add(jsonArray.optJSONObject(i).optString("id"));
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "hi" + getString(R.string.no_posts),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        requestIds.executeAsync();

        // grab likes
        for (int i = 0; i < NUM_POSTS; i++) {
            GraphRequest requestLikes = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                postIds.get(i) + "/likes",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                        if (jsonArray.length() != 0) {
                            for (int j = 0; j < jsonArray.length(); j++) {
                                String id = jsonArray.optJSONObject(j).optString("id");
                                if (!friendsToCount.containsKey(id)) {
                                    // create new friend
                                    friendsToCount.put(id, 1);
                                } else {
                                    // increment count
                                    friendsToCount.put(id, friendsToCount.get(id) + 1);
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.no_likers), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            requestLikes.executeAsync();
        }

        GraphRequest requestComments = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                postIds.get(0) + "/comments",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                        if (jsonArray.length() != 0) {
                            for (int j = 0; j < jsonArray.length(); j++) {
                                String id = jsonArray.optJSONObject(j).optJSONObject("from").optString("id");
                                if (!friendsToCount.containsKey(id)) {
                                    // create new friend
                                    friendsToCount.put(id, 1);
                                } else {
                                    // increment count
                                    friendsToCount.put(id, friendsToCount.get(id) + 1);
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.no_likers), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        requestComments.executeAsync();

        //store friends
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("users/" +
                FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "/friends");
        dataRef.setValue(friendsToCount);

    }

}
