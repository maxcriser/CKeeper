package com.maxcriser.cards.dialog;

import android.os.Bundle;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

public class AlertHandler {

    private static final String KEY_DIALOG_ID = "id";

    private SparseArray<WeakReference<AlertDialogHead<?>>> handledDialogs;

    public AlertHandler() {
        handledDialogs = new SparseArray<>();
    }

    public void saveInstanceState(Bundle outState) {
        for (int index = handledDialogs.size() - 1; index >= 0; index--) {
            WeakReference<AlertDialogHead<?>> dialogRef = handledDialogs.valueAt(index);
            if (dialogRef.get() == null) {
                handledDialogs.remove(index);
                continue;
            }
            AlertDialogHead<?> dialog = dialogRef.get();
            if (dialog.isShowing()) {
                dialog.onSaveInstanceState(outState);
                outState.putInt(KEY_DIALOG_ID, handledDialogs.keyAt(index));
                return;
            }
        }
    }

    void handleDialogStateSave(int id, AlertDialogHead<?> dialog) {
        handledDialogs.put(id, new WeakReference<AlertDialogHead<?>>(dialog));
    }

    public static boolean wasDialogOnScreen(Bundle savedInstanceState) {
        return savedInstanceState.keySet().contains(KEY_DIALOG_ID);
    }

    public static int getSavedDialogId(Bundle savedInstanceState) {
        return savedInstanceState.getInt(KEY_DIALOG_ID, -1);
    }
}
