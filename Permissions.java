import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


/**
 * <p>This class handles all permissions that are required at runtime for
 * devices running on API 23 (Marshmallow) or higher.</p>
 */
public class Permissions {

    // All permission constants that should be used by activities to request permission
    public static final int PERMISSIONS_REQUEST_CALL_PHONE = 100;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    private static final String TAG = Permissions.class.getSimpleName();
    private PermissionResponseListener permissionResponseListener;

    /**
     * <p>Request for permission at runtime for the requested operation.</p>
     * @param activity a reference to the calling activity
     * @param permissionCode integer constants to request permission
     * @param permissionResponseListener response listener for permission request
     */
    public void requestPermission(BaseActionBarActivity activity, int permissionCode, PermissionResponseListener permissionResponseListener) {
        this.permissionResponseListener = permissionResponseListener;

        activity.setPermissionResponseListener(permissionResponseListener);

        String permission = getPermissionString(permissionCode);

        // Here, thisActivity is the current activity
        if (permission != null
                && ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(activity, permissionCode)) {

                //TODO display a dialog with explanation message

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }
            // No explanation needed, we can request the permission.
            else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCode);
            }
        }
        else {
            permissionResponseListener.onRequestPermissionResponse(permissionCode, true);
        }
    }

    private boolean shouldShowRequestPermissionRationale(Activity activity, int permissionCode) {
        boolean shouldShow = false;
        switch (permissionCode) {
            case PERMISSIONS_REQUEST_CALL_PHONE:
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE))
                    shouldShow = false;
                break;

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION))
                    shouldShow = false;
                break;

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    shouldShow = false;
                break;
        }
        return shouldShow;
    }

    private String getPermissionString(int permissionCode) {
        String permissionString = null;
        switch (permissionCode) {
            case PERMISSIONS_REQUEST_CALL_PHONE:
                permissionString = Manifest.permission.CALL_PHONE;
                break;

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                permissionString = Manifest.permission.ACCESS_FINE_LOCATION;
                break;

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                permissionString = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
        }
        return permissionString;
    }
}
