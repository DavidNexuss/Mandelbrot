package nsoft.complex;


import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.omg.CORBA.portable.ValueInputStream;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MainGL {

	static class Col{float r,g,b;}
	
	static final int Width = 1920;
	static final int Height = 1080;
	

	public static final float it = 100;
	public static final int limit = 300;
	
	public static float xoffset = .5f;
	public static float yoffset = .5f;
	
	static float gridSize = 0.0003f;
	
	
	static Col[][] pix = new Col[Width][Height];
	// The window handle
	private long window;

	public void run() {
		
		init();
		generate();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static final int g = 10;
	public static final int f = 8;
	public static void generate() {
		
		setupPix();
		for (int i = 0; i < g; i++) {
			
			aplyFxaa();
		}
		
		
	}
	//Same thing I did in automata to aply an anti alias effect
	public static void fxaa (Col a,Col ... cols) {
		
		float b = 0;
		for (int i = 0; i < cols.length; i++) {
			
			b+= cols[i].r;
		}
		
		a.r = a.r + b/(g*f);
		a.g = a.g + b/(g*f);
		a.b = a.b + b/(g*f);
	}
	
	public static void aplyFxaa() {
		
		for (int i = 0; i < Width; i++) {
			
			for (int j = 0; j < Height; j++) {
				
				int mi = i-1;
				int mj = j-1;
				
				int li = (i+1) % Width;
				int lj = (j+1) % Height;
				
				if(mi < 0) mi= Width -1;
				if(mj < 0) mj = Height -1;
				
				fxaa(pix[i][j], pix[mi] [j],
						pix[i] [mj],
						pix[mi][mj],
						pix[li] [j],
						pix[i] [lj],
						pix[li] [lj]);

			}
		}
	}
	private static void setupPix() {
		
		for (int i = 0; i < Width; i++) {
			
			for (int j = 0; j < Height; j++) {
				
				pix[i][j] = new Col();
				
				float x = (i - Width/2)*gridSize + xoffset;
				float y = (j - Height/2)*gridSize + yoffset;
				
				float cx = x;
				float cy = y;
				
				int n = 0;
				
				while(n < it) {
					
					float xx = x*x - y*y;
					float yy = 2 * x * y;
					
					x = xx + cx;
					y = yy + cy;
					n++;
					
					if(x + y > limit) {
						
						break;
					}
					
				}
					pix[i] [j].r =n/it;
					pix[i] [j].g =n/it;
					pix[i] [j].b =n/it;
				
			}
		}
	}
	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		
	    
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(Width, Height, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); 
			else if(key == GLFW_KEY_UP && action == GLFW_RELEASE) {
				
				yoffset += .05f;
				generate();
			}else if(key == GLFW_KEY_DOWN && action == GLFW_RELEASE) {
				
				yoffset -= .05f;
				generate();
			}else if(key == GLFW_KEY_RIGHT && action == GLFW_RELEASE) {
				
				xoffset += .05f;
				System.out.println("start");
				generate();
				System.out.println("end");
			}else if(key == GLFW_KEY_LEFT && action == GLFW_RELEASE) {
				
				xoffset -= .05f;
				generate();
			}	
				
			else if(key == GLFW_KEY_4 && action == GLFW_RELEASE) {
			
				gridSize -= 0.0003f;
				generate();
			}else if(key == GLFW_KEY_1 && action == GLFW_RELEASE) {
		
				gridSize += 0.0003f;
				generate();
			}
			
			
		});

		
		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		
		
		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, Width, 0, Height, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			

			glClear(GL_COLOR_BUFFER_BIT);
			
			for (int i = 0; i < Width; i++) {
				
				for (int j = 0; j < Height; j++) {
					
					glColor3f(pix[i][j].r,pix[i][j].g,pix[i][j].b);
					glBegin(GL_QUADS);
					
					glVertex2f(i, j);
					glVertex2f((i + 1), j);
					glVertex2f((i + 1), (j + 1));
					glVertex2f(i, (j +1));
					
					glEnd();
					
				}
			}
			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
		
		System.out.println("exit");
		System.exit(0);
	}

	public static void main(String[] args) {
		new MainGL().run();
	}

}
