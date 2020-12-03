package com.android.casebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;

public class ViewSelectedDate extends AppCompatActivity {
    private static final String TAG = "TAG";
    CardView InnerCaseBtn;
    ProgressBar casesProgress;
    LinearLayout noCasesFound;

    private FirebaseFirestore fStore;
    private RecyclerView myCasesView;
    private FirebaseAuth fAuth;
    private FirestoreRecyclerAdapter adapter;

    Calendar calendar = Calendar.getInstance();
    Date todayDate = calendar.getTime();
    SimpleDateFormat checkFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    String todayFormattedDate = checkFormat.format(todayDate);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_ns);

        final String dateSelected = getIntent().getStringExtra("SelectedDate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        myCasesView = findViewById(R.id.casesList);
/*        InnerCaseBtn = findViewById(R.id.inner_case_btn);*/
        casesProgress = findViewById(R.id.casesProgress);
        noCasesFound = findViewById(R.id.noCasesFound);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        final String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query query = fStore.collection("users").document(userID).collection("casesID")
                .whereEqualTo("dispose", false);

        assert dateSelected != null;
        if (dateSelected.equals(todayFormattedDate)) {

/*            query.whereEqualTo("")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                    }
                }
            });
*/
            Intent intent = new Intent(getApplicationContext(), TabbedDates.class);
            intent.putExtra("Something", "SelectedDate");
            startActivity(intent);

            /*query.whereEqualTo("prev_date", dateSelected);
            neutralView(query);*/



        } else {
            neutralView(query.whereEqualTo("case_date", dateSelected));
        }

    }

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

        adapter = new FirestoreRecyclerAdapter<ModelCaseView, ViewSelectedDate.CaseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewSelectedDate.CaseViewHolder caseViewHolder, int i, @NonNull ModelCaseView modelCaseView) {
                caseViewHolder.case_title.setText(modelCaseView.getCase_title());
                caseViewHolder.party_name.setText(modelCaseView.getParty_name());
                caseViewHolder.case_date.setText(modelCaseView.getCase_date());
                casesProgress.setVisibility(View.GONE);
                caseViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(caseViewHolder.getLayoutPosition());
                        String caseID = snapshot.getId();

                        Log.d(TAG, "Position is : " + caseViewHolder.getLayoutPosition() + " Click is Working " + caseID);

                        Intent intent = new Intent(ViewSelectedDate.this, ViewInnerCase.class);
                        intent.putExtra("case_ID", caseID);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ViewSelectedDate.CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_single_case, parent, false);
                return new ViewSelectedDate.CaseViewHolder(view);
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