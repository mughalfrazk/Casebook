package com.android.casebook;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnListItemClick {
    void onItemClick(DocumentSnapshot snapshot, int position);
}
