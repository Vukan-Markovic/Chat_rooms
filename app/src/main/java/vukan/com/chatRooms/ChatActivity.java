package vukan.com.chatRooms;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class ChatActivity extends AppCompatActivity {
    private static final String ANONYMOUS = "anonymous";
    private static final int DEFAULT_MSG_LENGTH_LIMIT = 500;
    private MessageAdapter mMessageAdapter;
    private EditText mMessageEnter;
    private Button mSendButton;
    private String mUsername;
    private SoundHelper mSound;
    private DatabaseReference mMessagesDatabaseReference;
    @Nullable
    private ChildEventListener mChildEventListener;
    private Intent mIntent;
    private String mMessage;
    private List<String> mWords;
    private Animation mAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mIntent = getIntent();
        mUsername = ANONYMOUS;
        mWords = Arrays.asList(getResources().getStringArray(R.array.bad_words));
        if (mIntent.hasExtra(SubCategoriesActivity.CATEGORY) && mIntent.hasExtra(SubCategoriesActivity.SUBCATEGORY))
            mMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(mIntent.getStringExtra(SubCategoriesActivity.CATEGORY) + "/" + mIntent.getStringExtra(SubCategoriesActivity.SUBCATEGORY));
        mMessagesDatabaseReference.keepSynced(true);
        setTitle(mIntent.getStringExtra(SubCategoriesActivity.SUBCATEGORY));
        ListView mMessageListView = findViewById(R.id.message_list_view);
        ViewGroup relativeLayout = findViewById(R.id.relative_layout);
        mMessageEnter = findViewById(R.id.messageEnter);
        mSendButton = findViewById(R.id.send);
        List<Message> messages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, messages);
        mMessageListView.setAdapter(mMessageAdapter);
        if (mIntent.hasExtra(SubCategoriesActivity.USER))
            mUsername = mIntent.getStringExtra(SubCategoriesActivity.USER);
        mSound = new SoundHelper(this);
        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        mAnimation.setDuration(200);
        attachDatabaseReadListener();

        mMessageEnter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(@NonNull CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) mSendButton.setEnabled(true);
                else mSendButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mMessageEnter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mSendButton.setOnClickListener(view -> {
            view.startAnimation(mAnimation);
            if (mMessageEnter.length() != 0 && !mMessageEnter.getText().toString().equals("") && !mMessageEnter.getText().toString().isEmpty()) {
                mMessage = mMessageEnter.getText().toString().trim();
                for (String word : mWords)
                    mMessage = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE).matcher(mMessage).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
                if (mIntent.getData() != null)
                    mMessagesDatabaseReference.push().setValue(new Message(mMessage, mUsername, mIntent.getData().toString(), Calendar.getInstance().getTime().toString()));
                mSound.playSound();
                mMessageEnter.setText("");
            }
        });

        if (mIntent.hasExtra(SubCategoriesActivity.CATEGORY)) {
            switch (mIntent.getStringExtra(SubCategoriesActivity.CATEGORY)) {
                case "games":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background);
                    break;
                case "economy":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_1);
                    break;
                case "movies":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_2);
                    break;
                case "technology":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_3);
                    break;
                case "art":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_4);
                    break;
                case "music":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_5);
                    break;
                case "sport":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_6);
                    break;
                case "series":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_7);
                    break;
                case "countries":
                    relativeLayout.setBackgroundResource(R.drawable.chat_background_8);
                    break;
                default:
                    relativeLayout.setBackgroundResource(R.drawable.chat_background);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mMessageAdapter.add(dataSnapshot.getValue(Message.class));
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}