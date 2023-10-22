import gdp.stdlib.*;
import java.awt.Color;

public class Sweeper {
	
	static final Color[] FARBEN = new Color[] {
		new Color(0,0,255), new Color(0,127,0),
	    new Color(255,0,0), new Color(0,0,127),
	    new Color(127,0,0), new Color(0,127,127),
	    new Color(0,0,0),   new Color(127,127,127)};
	
	static final int DELAY = 0; // HIER DELAY PRO SCHRITT IN MILLISEKUNDEN ANGEBEN; 0 IST MINIMUM (5-10 ist ganz okay um es etwas langsamer zu sehen)
	
	private static class Feld {
		private final boolean bomb;
		private final int number;
		private final int x, y;
		private boolean open = false;
		private boolean marked = false;
		private boolean abgegessen = false;
		
		private Feld (boolean bomb, int number, int x, int y) {
			this.bomb = bomb;
			this.number = number;
			this.x = x;
			this.y = y;
		}
		
		// Setter
		
		public void open() { this.open = true; }
		
		public void mark() { this.marked = true; }
		
		public void essen() { this.abgegessen = true; }
		
		// Getter
		
		public boolean Bomb() { return this.bomb; }
		
		public int Number() { return this.number; }
		
		public boolean Marked() { return this.marked; }
		
		public boolean IsOpen() { return this.open; }
		
		public int x() { return this.x; }
		
		public int y() { return this.y; }
		
		public boolean gegessen() { return this.abgegessen; }
		
		// Magic
		
		public String toString() {
			return this.open ? (this.bomb ? "*" : this.number != 0 ? this.number + "" : " ") : (this.marked ? "⚑" : "?");
		}
	}
	
	public static void main(String[] args) {
		
		// Einlesen
		
		final int X = StdIn.readInt();
		final int Y = StdIn.readInt();
		int bomben = StdIn.readInt();
		int startX = StdIn.readInt();
		int startY = StdIn.readInt(); 
		int anzFelder = X*Y - CEC(startX, startY, X, Y)-1;
		StdDraw.setCanvasSize(30*X, 30*Y);
		StdDraw.setXscale(0, X);
		StdDraw.setYscale(0, Y);
		
		Feld[][] feld = new Feld[Y][X];
		
		// Felder festlegen
		
		for (int i = 0; i < Y; i++) {
			for (int j = 0; j < X; j++) {
				boolean darfErDas = true;
				for (int ii = -1; ii <= 1; ii++) {
					for (int jj = -1; jj <= 1; jj++) {
						if (startY + ii == i && startX + jj == j) darfErDas = false;
					}
				}
				if (darfErDas) {
					int r = randint(1,anzFelder);
					if (r <= bomben) {
						feld[i][j] = new Feld(true, 0, j, i);
						bomben--;
					}
					anzFelder--;
				}
			}
		}
		
		for (int i = 0; i < Y; i++) {
			for (int j = 0; j < X; j++) {
				if (feld[i][j] == null) {
					Feld[] drumherum = getFelds(feld, j, i);
					int c = 0;
					for (int k = 0; k < drumherum.length; k++) {
						if (!(drumherum[k] == null) && drumherum[k].Bomb()) {
							c++;
						}
					}
					feld[i][j] = new Feld(false, c, j, i);
				}
			}
		}
		
		// anfangen zu öffnen
		feld[startY][startX].open();
		draw(feld);
		StdDraw.show();
		
		// lösen
		basicSolve(feld);
		printGame(feld);
		draw(feld);
		StdDraw.show(DELAY);
		//StdOut.println();
	}
	
	// ---- PRINTS THE CURRENT STATE OF THE GAME IN THE CONSOLE ----
	private static void printGame(Feld[][] feld) {
		for (int i = 0; i < feld[0].length; i++) {
			StdOut.print("___");
		}
		StdOut.println();
		for (Feld[] Y : feld) {
			StdOut.print("|");
			for (Feld X : Y) {
				StdOut.print(" " + X + " ");
			}
			StdOut.println();
		}
		StdOut.println();
	}
	
	// ---- BASIC SOLVE ----
	private static void basicSolve(Feld[][] feld) {
		boolean aktiv = true;
		// ---------
		// MAIN DING
		// ---------
		
		while (!isSolved(feld) && !isFailed(feld) && aktiv) {
			
			boolean aktiv2 = true;
			
			// solang aufmachen bis nich mehr geht
			do {
				aktiv2 = aufmachen(feld);
			} while (aktiv2);
			
			// neue markierungen machen
			aktiv = markieren(feld);
		}
		
		// --------------
		// MAIN DING ENDE
		// --------------
		
		if (isSolved(feld)) {
			for (Feld[] zeile : feld) {
				for (Feld e : zeile) {
					if (e.Bomb() && !e.Marked()) e.mark();
				}
			}
		}
		else if (isFailed(feld)) {
			for (Feld[] zeile : feld) {
				for (Feld e : zeile) {
					if (e.Bomb() && !e.Marked()) e.open();
				}
			}
		} else {
			StdOut.println("RIP, game stuck");
		}
	}
	
	// ---- MARKIEREN ----
	private static boolean markieren(Feld[][] feld) {
		int längeX = feld[0].length; int längeY = feld.length;
		boolean erg = false;
		for (int y = 0; y < längeY; y++) {
			for (int x = 0; x < längeX; x++) {
				
				if (feld[y][x].IsOpen() && !feld[y][x].gegessen()) {
					Feld[] drumherum = getFelds(feld, x, y);
					
					int n1 = 0;
					for (Feld e : drumherum) {
						if (!e.IsOpen()) n1++;
					}
					int n2 = feld[y][x].Number();
					
					if (n1 == n2) {
						for (Feld e : drumherum) {
							if (!e.IsOpen()) {
								e.mark();
								erg = true;
								feld[y][x].essen();
								drawFeld(feld, e.x(), e.y());
								StdDraw.show(DELAY);
							}
						}
					}
				}
			}
		}
		return erg;
	}
	
	// ---- AUFMACHEN ----
	private static boolean aufmachen(Feld[][] feld) {
		int längeX = feld[0].length; int längeY = feld.length;
		boolean erg = false;
		for (int y = 0; y < längeY; y++) {
			for (int x = 0; x < längeX; x++) {
				
				if (feld[y][x].IsOpen() && !feld[y][x].gegessen()) {
					
					Feld[] drumherum = getFelds(feld, x, y);
					int n1 = 0;
					for (Feld e : drumherum) {
						if (e.Marked()) { n1++; }
					}
					int n2 = feld[y][x].Number();
					
					if (n1 == n2) {
						for (Feld e : drumherum) {
							if (!e.Marked() && !e.IsOpen()) {
								e.open();
								feld[y][x].essen();
								erg = true;
								drawFeld(feld, e.x(), e.y());
								StdDraw.show(DELAY);
							}
						}
					}
				}
			}
		}
		return erg;
	}
	
	// ---- DRAW IN STDDRAW ----
	private static void draw(Feld[][] feld) {
		for (int i = 0; i < feld.length; i++) {
			for (int j = 0; j < feld[0].length; j++) {
				drawFeld(feld, j, i);
			}
		}
	}
	
	private static void drawFeld(Feld[][] feld, int x, int y) {
		Feld meinFeld = feld[y][x];
		if (!meinFeld.IsOpen()) { StdDraw.setPenColor(StdDraw.DARK_GRAY); }
		else { StdDraw.setPenColor(StdDraw.GRAY); }
		
		// sq
		StdDraw.filledSquare(x+0.5, feld.length-y-0.5, 0.5);
		
		String txt = feld[y][x].toString();
		if (!txt.equals(" ") && !txt.equals("⚑") && !txt.equals("*") && !txt.equals("?")) {
			StdDraw.setPenColor(FARBEN[Integer.parseInt(txt)-1]);
		} else if (txt.equals("⚑")) {
			StdDraw.setPenColor(StdDraw.RED);
		} else { StdDraw.setPenColor(StdDraw.BLACK); }
		
		// txt
		StdDraw.text(x+0.5, feld.length-y-0.5, txt);
	}
	
	// ---- IS IT SOLVED? ----
	private static boolean isSolved(Feld[][] feld) {
		for (Feld[] zeile : feld) {
			for (Feld e : zeile) {
				if (!e.IsOpen() && !e.Bomb()) return false;
			}
		}
		return true;
	}
	
	// ---- IS IT FAILED? ----
	private static boolean isFailed(Feld[][] feld) {
		for (Feld[] zeile : feld) {
			for (Feld e : zeile) {
				if (e.IsOpen() && e.Bomb()) return true;
			}
		}
		return false;
	}
	
	// ---- GET THE FELDS AROUND A SPECIFIC COORDINATE ----
	private static Feld[] getFelds(Feld[][] feld, int x, int y) {
		int längeX = feld[0].length; int längeY = feld.length;
		Feld[] erg = new Feld[CEC(x, y, längeX, längeY)];
		int c = 0;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (inBounds(x+j, y+i, längeX, längeY)) {
					if (!(feld[y][x] == feld[y+i][x+j])) {
						erg[c] = feld[y+i][x+j];
						c++;
					}
				}
			}
		}
		
		return erg;
	}
	
	// ---- HELP FUNCTION FOR COORDINATES BEING ON AN EDGE OR IN A CORNER ----
	private static int CEC(int x, int y, int längeX, int längeY) {
		if ((x == 0 || x == längeX-1) && (y == 0 || y == längeY-1)) {
			return 3;
		} else if (x == 0 || x == längeX-1 || y == 0 || y == längeY-1) {
			return 5;
		} else {
			return 8;
		}
	}
	
	// ---- ARE THE COORDS INBOUNDS? ----
	private static boolean inBounds(int x, int y, int längeX, int längeY) {
		return (x >= 0 && x < längeX && y >= 0 && y < längeY) ? true : false;
	}
	
	// ---- BASIC RANDINT FUNCTION ----
	public static int randint(int anfang, int ende) {
		return (int) (Math.random() * (ende-anfang+1) + anfang);
	}

}
