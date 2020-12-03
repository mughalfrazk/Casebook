package com.android.casebook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ViewClients extends AppCompatActivity {
    private static final String TAG = "TAG";
    private static final int REQUEST_PHONE_CALL = 1;

    private FirebaseFirestore fStore;
    private RecyclerView myCasesView;
    private FirebaseAuth fAuth;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_view);

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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_field_clients, parent, false);
                return new ViewClients.ClientsHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ClientsHolder clientsHolder, int i, @NonNull ModelCaseView modelCaseView) {
                clientsHolder.party_name.setText(modelCaseView.getParty_name());
                clientsHolder.client_contact.setText(modelCaseView.getClient_contact());

                clientsHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(clientsHolder.getLayoutPosition());
                        String phoneNumber = snapshot.getString("client_contact");

                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:"+phoneNumber));

                        if (ContextCompat.checkSelfPermission(ViewClients.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ViewClients.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                        }
                        else
                        {
                            startActivity(callIntent);
                        }
                    }
                });
            }
        };

        myCasesView.setHasFixedSize(true);
        myCasesView.setLayoutManager(new LinearLayoutManager(this));
        myCasesView.setAdapter(adapter);
    }

    private class ClientsHolder extends RecyclerView.ViewHolder {
        private TextView party_name, client_contact;

        public ClientsHolder(@NonNull View itemView) {
            super(itemView);
            party_name = itemView.findViewById(R.id.txtField);
            client_contact = itemView.findViewById(R.id.txtFieldNum);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}