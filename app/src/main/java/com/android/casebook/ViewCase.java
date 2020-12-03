package com.android.casebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class ViewCase extends AppCompatActivity {
    private static final String TAG = "TAG";
    CardView InnerCaseBtn;
    AutoCompleteTextView searchBar;
    EditText SearchEditText;
    ProgressBar casesProgress;
    ImageButton searchBTN;
    TextView searchCancel;
    LinearLayout noCasesFound;

    private FirebaseFirestore fStore;
    private RecyclerView myCasesView;
    private FirebaseAuth fAuth;
    FirestoreRecyclerAdapter adapter;
    CollectionReference collectionReference;
    String searchText;
    String userID;
    Query query, SearchQuery;

    String[] titleArray, partyArray, searchArray;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        String incomingIntent = getIntent().getStringExtra("deleteID");

        myCasesView = findViewById(R.id.casesList);
/*        InnerCaseBtn = findViewById(R.id.inner_case_btn);*/
        casesProgress = findViewById(R.id.casesProgress);
        searchBTN = findViewById(R.id.searchCase);
        SearchEditText = findViewById(R.id.searchBar);
        searchCancel = findViewById(R.id.searchCancel);
        searchBar = (AutoCompleteTextView) findViewById(R.id.searchBar);
        noCasesFound = findViewById(R.id.noCasesFound);

        casesProgress.setVisibility(View.VISIBLE);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        assert currentUser != null;
        userID = currentUser.getUid();

        collectionReference = fStore.collection("users").document(userID).collection("casesID");

        if (incomingIntent != null) {
            collectionReference.document(incomingIntent).delete();
        }

        query = collectionReference.whereEqualTo("dispose", false);
        neutralView(query);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int caseCount = Objects.requireNonNull(task.getResult()).size();

                if (task.isSuccessful() && caseCount > 0) {

                    titleArray = new String[caseCount];
                    partyArray = new String[caseCount];
                    int increment = 0;
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        titleArray[increment] = documentSnapshot.getString("case_title");
                        increment++;
                    }


                    searchBar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchBTN.setVisibility(View.VISIBLE);
                            searchCancel.setVisibility(View.GONE);
                        }
                    });
                    searchBar.setAdapter(new ArrayAdapter<>(ViewCase.this, android.R.layout.simple_list_item_1, titleArray));

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchText = SearchEditText.getText().toString().trim();
                if (searchText.matches("")) {
                    Toast.makeText(ViewCase.this, "Nothing to Search" + searchText, Toast.LENGTH_SHORT).show();
                }
                else {
                    SearchQuery = query.whereEqualTo("case_title", searchText);
                    SearchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                neutralView(SearchQuery);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: "+ e.getMessage());
                        }
                    });

                    searchBTN.setVisibility(View.GONE);
                    searchCancel.setVisibility(View.VISIBLE);
                }
            }
        });


////  CANCEL SEARCH  ////

        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText = SearchEditText.getText().toString().trim();
                if (searchText.matches("")) {
                    Toast.makeText(ViewCase.this, "Nothing to Search" + searchText, Toast.LENGTH_SHORT).show();
                }
                else {
                    SearchQuery = query.whereEqualTo("case_title", searchText);
                    SearchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                neutralView(SearchQuery);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: "+ e.getMessage());
                        }
                    });

                    searchBTN.setVisibility(View.GONE);
                    searchCancel.setVisibility(View.VISIBLE);
                }
            }
        });


////  CANCEL SEARCH  ////

        searchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBTN.setVisibility(View.VISIBLE);
                searchCancel.setVisibility(View.GONE);
                SearchEditText.getText().clear();
                query = collectionReference.whereEqualTo("dispose", false);
                neutralView(query);
            }
        });
    }


////  CASE RECYCLER VIEW  ////

    private void neutralView(final Query query) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && Objects.requireNonNull(task.getResult()).size() != 0) {
                    CaseRecyclerView(query);
                } else if (Objects.requireNonNull(task.getResult()).size() == 0){
                    casesProgress.setVisibility(View.GONE);
                    noCasesFound.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void CaseRecyclerView(Query query) {
        FirestoreRecyclerOptions<ModelCaseView> options = new FirestoreRecyclerOptions.Builder<ModelCaseView>()
                .setLifecycleOwner(this)
                .setQuery(query, ModelCaseView.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ModelCaseView, CaseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CaseViewHolder caseViewHolder, int i, @NonNull ModelCaseView modelCaseView) {
                caseViewHolder.case_title.setText(modelCaseView.getCase_title());
                caseViewHolder.party_name.setText(modelCaseView.getParty_name());
                caseViewHolder.case_date.setText(modelCaseView.getCase_date());
                casesProgress.setVisibility(View.GONE);
                caseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(caseViewHolder.getLayoutPosition());
                        String caseID = snapshot.getId();

                        Intent intent = new Intent(ViewCase.this, ViewInnerCase.class);
                        intent.putExtra("case_ID", caseID);
                        startActivity(intent);
                    }
                });
            }


            @NonNull
            @Override
            public CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_single_case, parent, false);
                return new CaseViewHolder(view);
            }
        };

        myCasesView.setHasFixedSize(true);
        myCasesView.setLayoutManager(new LinearLayoutManager(this));
        myCasesView.setAdapter(adapter);
    }

    private static class CaseViewHolder extends RecyclerView.ViewHolder {

        TextView case_title, party_name, case_date;

        public CaseViewHolder(@NonNull View itemView) {
            super(itemView);

            case_title = itemView.findViewById(R.id.txttitle);
            party_name = itemView.findViewById(R.id.txtclient);
            case_date = itemView.findViewById(R.id.txtdate);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}