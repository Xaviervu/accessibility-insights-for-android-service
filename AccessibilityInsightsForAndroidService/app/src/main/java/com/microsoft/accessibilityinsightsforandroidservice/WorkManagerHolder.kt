// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice;

import android.content.Context;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import java.util.WeakHashMap;

public class WorkManagerHolder {
  private static final Object LockObject = new Object();
  private static final WeakHashMap<Context, WorkManager> ContextToManagerMap =
      new WeakHashMap<>();

  public static WorkManager getWorkManager(Context context) {
    synchronized (LockObject) {
      WorkManager managerForContext = ContextToManagerMap.get(context);

      if (managerForContext == null) {
        try {
          managerForContext = WorkManager.getInstance(context);
        } catch (IllegalStateException e) {
          try {
            WorkManager.initialize(context, new Configuration.Builder().build());
          } catch (IllegalStateException e2) {
            // In case it was initialized between getInstance and initialize calls
          }
          managerForContext = WorkManager.getInstance(context);
        }
        ContextToManagerMap.put(context, managerForContext);
      }
      return managerForContext;
    }
  }
}
