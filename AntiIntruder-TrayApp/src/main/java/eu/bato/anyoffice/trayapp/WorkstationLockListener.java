package eu.bato.anyoffice.trayapp;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinUser.WNDCLASSEX;
import com.sun.jna.platform.win32.WinUser.WindowProc;
import com.sun.jna.platform.win32.Wtsapi32;
import org.slf4j.LoggerFactory;

public class WorkstationLockListener implements WindowProc {
    
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(WorkstationLockListener.class);
    
    private final HWND hWnd;
    private final WString windowClass;
    private final HMODULE hInst;

    /**
     * Instantiates a new win32 window test.
     */
    public WorkstationLockListener() {
        // define new window class
        windowClass = new WString("MyWindowClass");
        hInst = Kernel32.INSTANCE.GetModuleHandle("");

        WNDCLASSEX wClass = new WNDCLASSEX();
        wClass.hInstance = hInst;
        wClass.lpfnWndProc = WorkstationLockListener.this;
        wClass.lpszClassName = windowClass;

        // register window class
        User32.INSTANCE.RegisterClassEx(wClass);
        getLastError();

        // create new window
        hWnd = User32.INSTANCE.CreateWindowEx(User32.WS_EX_TOPMOST, windowClass, "'TimeTracker hidden helper window to catch Windows events", 0, 0, 0, 0, 0, null, // WM_DEVICECHANGE contradicts parent=WinUser.HWND_MESSAGE
                null, hInst, null);

        getLastError();
        log.debug("window sucessfully created! window hwnd: " + hWnd.getPointer().toString());

        Wtsapi32.INSTANCE.WTSRegisterSessionNotification(hWnd, Wtsapi32.NOTIFY_FOR_THIS_SESSION);

        MSG msg = new MSG();
        while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) != 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);
        }
    }
    
    public void destroy(){
        Wtsapi32.INSTANCE.WTSUnRegisterSessionNotification(hWnd);
        User32.INSTANCE.UnregisterClass(windowClass, hInst);
        User32.INSTANCE.DestroyWindow(hWnd);
        log.debug("program exit!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.jna.platform.win32.User32.WindowProc#callback(com.sun.jna.platform .win32.WinDef.HWND, int, com.sun.jna.platform.win32.WinDef.WPARAM, com.sun.jna.platform.win32.WinDef.LPARAM)
     */
    @Override
    public LRESULT callback(HWND hwnd, int uMsg, WPARAM wParam, LPARAM lParam) {
        switch (uMsg) {
            case WinUser.WM_QUIT: {
                PersonStateManager.getInstance().setState(PersonState.UNKNOWN);
                return new LRESULT(0);
            }
            case WinUser.WM_DESTROY: {
                User32.INSTANCE.PostQuitMessage(0);
                return new LRESULT(0);
            }
            case WinUser.WM_SESSION_CHANGE: {
                this.onSessionChange(wParam, lParam);
                return new LRESULT(0);
            }
            default:
                return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
        }
    }

    /**
     * Gets the last error.
     *
     * @return the last error
     */
    public int getLastError() {
        int rc = Kernel32.INSTANCE.GetLastError();

        if (rc != 0) {
            log.error("error: " + rc);
        }

        return rc;
    }

    /**
     * On session change.
     *
     * @param wParam the w parameter
     * @param lParam the l parameter
     */
    private void onSessionChange(WPARAM wParam, LPARAM lParam) {
        switch (wParam.intValue()) {
            case Wtsapi32.WTS_SESSION_LOCK: 
            case Wtsapi32.WTS_SESSION_LOGOFF:
                this.onMachineLocked(lParam.intValue());
                break;
            case Wtsapi32.WTS_SESSION_UNLOCK:
            case Wtsapi32.WTS_SESSION_LOGON:
                this.onMachineUnlocked(lParam.intValue());
                break;
        }
    }

    /**
     * On machine locked.
     *
     * @param sessionId the session id
     */
    private void onMachineLocked(int sessionId) {
        log.info("Machine locked right now!");
        PersonStateManager.getInstance().workstationLock();
    }

    /**
     * On machine unlocked.
     *
     * @param sessionId the session id
     */
    private void onMachineUnlocked(int sessionId) {
        log.info("Machine unlocked right now!");
        TrayIconManager.getInstance().updateState(PersonStateManager.getInstance().workstationUnlock());
    }
}
