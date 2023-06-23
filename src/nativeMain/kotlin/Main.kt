import kotlinx.cinterop.*
import platform.windows.*

@ExperimentalUnsignedTypes
fun WndProc(hwnd: HWND?, msg: UINT, wParam: WPARAM, lParam: LPARAM) : LRESULT
{



    // This switch block differentiates between the message type that could have been received. If you want to
    // handle a specific type of message in your application, just define it in this block.
    when(msg)
    {
        // This message type is used by the OS to close a window. Just closes the window using DestroyWindow(hwnd);
        WM_CLOSE.toUInt() -> DestroyWindow(hwnd)

        // This message type is part of the WM_CLOSE case. After the DestroyWindow(hwnd) function is called, a
        // WM_DESTROY message is sent to the window, which actually closes it.
        WM_DESTROY.toUInt() -> PostQuitMessage(0)

        // This message type is an important one for GUI programming. It symbolizes an event for a button for example.
        WM_COMMAND.toUInt() ->
        {

            if (wParam == 18UL)
            {
//                 MessageBoxA(hwnd, "Button is pressed!", "test", MB_ICONINFORMATION);


                memScoped {
                    ShellExecute?.let {
                        it(hwnd, "open".wcstr.ptr,
                            "C:\\Windows\\System32\\notepad.exe".wcstr.ptr, null, null, SW_SHOWNORMAL)
                    }
                }



             }


        }



        WM_CREATE.toUInt() ->
        {

            val tacos = 18L.toCPointer<CPointed>()


            val hwndButton = CreateWindowExA(
                HTMAXBUTTON,
                "BUTTON",  // Predefined class; Unicode assumed
                "OK pizza",      // Button text
                (WS_TABSTOP or WS_VISIBLE or WS_CHILD or BS_DEFPUSHBUTTON).toUInt(),  // Styles
                10,         // x position
                10,         // y position
                100,        // Button width
                100,        // Button height
                hwnd,     // Parent window
                (tacos as CPointer<HMENU__>),
                GetWindowLongPtr!!(hwnd, GWLP_HINSTANCE).toCPointer(),
                NULL);      // Pointer not needed.



            val asdf = CreateWindowExA(
                HTMAXBUTTON,
                "BUTTON",  // Predefined class; Unicode assumed
                "OK",      // Button text
                (WS_TABSTOP or WS_VISIBLE or WS_CHILD or BS_DEFPUSHBUTTON).toUInt(),  // Styles
                90,         // x position
                90,         // y position
                100,        // Button width
                100,        // Button height
                hwnd,     // Parent window
                null,
                GetWindowLongPtr!!(hwnd, GWLP_HINSTANCE).toCPointer(),
                NULL);      // Pointer not needed.



            return 0;

        }





        // When no message type is handled in your application, return the default window procedure. In this case the message
        // will be handled elsewhere or not handled at all.
        else -> return (DefWindowProc!!)(hwnd, msg, wParam, lParam)
    }

    return 0;
}

@ExperimentalUnsignedTypes
fun main() {

    println("Hello, Kotlin/Native!")

//    val poop: Long = 0x3487da
//    val tacos = poop.toCPointer<CPointed>()
//    tacos.rawValue


    memScoped {

        val hInstance = (GetModuleHandle!!)(null)
        val lpszClassName = "LEDAK"

        // In order to be able to create a window you need to have a window class available. A window class can be created for your
        // application by registering one. The following struct declaration and fill provides details for a new window class.
        val wc = alloc<WNDCLASSEX>();

        wc.cbSize        = sizeOf<WNDCLASSEX>().toUInt();
        wc.style         = 0u;
        wc.lpfnWndProc   = staticCFunction(::WndProc)
        wc.cbClsExtra    = 0;
        wc.cbWndExtra    = 0;
        wc.hInstance     = hInstance;
        wc.hIcon         = null;
        wc.hCursor       = (LoadCursor!!)(hInstance, IDC_ARROW);


//        val bBrushBits = uintArrayOf(0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu, 0xFFu)
//        val hbm = CreateBitmap(8, 8, 1, 1, bBrushBits.toCValues())
//        wc.hbrBackground = CreatePatternBrush(hbm)


        // wc.hbrBackground = HBRUSH(COLOR_WINDOW+1);


        wc.hbrBackground = CreateSolidBrush(0xFF00FF)



        wc.lpszMenuName  = null;
        wc.lpszClassName = lpszClassName.wcstr.ptr
        wc.hIconSm       = null;

        // This function actually registers the window class. If the information specified in the 'wc' struct is correct,
        // the window class should be created and no error is returned.
        if((RegisterClassEx!!)(wc.ptr) == 0u.toUShort())
        {
            println("Failed to register!")
            return
        }

        // This function creates the first window. It uses the window class registered in the first part, and takes a title,
        // style and position/size parameters. For more information about style-specific definitions, refer to the MSDN where
        // extended documentation is available.
        val hwnd = CreateWindowExA(
            WS_EX_CLIENTEDGE,                                                               // Optional window styles.
            lpszClassName,                                                                  // Window class
            "Stepmania Switcher",                                                           // Window text
            (WS_OVERLAPPED or WS_CAPTION or WS_SYSMENU or WS_MINIMIZEBOX).toUInt(),         // Window style

            // Size and position
            CW_USEDEFAULT, CW_USEDEFAULT, 1400, 800,

            null,               // Parent window
            null,               // Menu
            hInstance,          // Instance handle
            NULL                // Additional application data
        )

        // Everything went right, show the window including all controls.
        ShowWindow(hwnd, 1);
        UpdateWindow(hwnd);


        // This part is the "message loop". This loop ensures the application keeps running and makes the window able to receive messages
        // in the WndProc function. You must have this piece of code in your GUI application if you want it to run properly.
        val Msg = alloc<MSG>();
        while((GetMessage!!)(Msg.ptr, null, 0u, 0u) > 0)
        {
            TranslateMessage(Msg.ptr);
            (DispatchMessage!!)(Msg.ptr);
        }
    }

}