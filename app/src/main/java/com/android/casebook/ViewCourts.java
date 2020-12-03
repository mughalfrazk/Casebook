package com.android.casebook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ViewCourts extends AppCompatActivity {
    private static final String TAG = "TAG";

    private FirebaseFirestore fStore;
    private RecyclerView myCasesView;
    private FirebaseAuth fAuth;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        myCasesView = findViewById(R.id.casesList);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        final String userID = currentUser.getUid();

        Query query = fStore.collection("users").document(userID).collection("casesID");

        FirestoreRecyclerOptions<ModelCaseView> options = new FirestoreRecyclerOptions.Builder<ModelCaseView>()
                .setLifecycleOwner(this)
                .setQuery(query, ModelCaseView.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ModelCaseView, ClientsHolder>(options) {

            @NonNull
            @Override
            public ClientsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_field, parent, false);
                return new ViewCourts.ClientsHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ClientsHolder clientsHolder, int i, @NonNull ModelCaseView modelCaseView) {
                clientsHolder.court_name.setText(modelCaseView.getCourt_name());
            }
        };

        myCasesView.setHasFixedSize(true);
        myCasesView.setLayoutManager(new LinearLayoutManager(this));
        myCasesView.setAdapter(adapter);
    }

    private class ClientsHolder extends RecyclerView.ViewHolder {
        private TextView court_name;

        public ClientsHolder(@NonNull View itemView) {
            super(itemView);
            court_name = itemView.findViewById(R.id.txtField);

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}