/*
 * Author: Lavi Johnson
 * Description: This is a Sudoku game application to be run on the command line.
 * If you wish to test its functionality, I can E-mail a test file to use with it,
 * or a puzzle can be created by simply naming a text file 'Test.txt'. The format for
 * the puzzle should be like the example that follows:
 *
 * 1 0 3 0 0 2 0 4 7
 * 0 0 0 0 0 3 0 0 0
 * 4 0 0 1 0 0 2 0 8
 * 0 2 8 4 9 0 0 0 0
 * 5 0 7 0 0 0 0 1 0
 * 3 0 0 5 0 1 0 0 0
 * 0 0 6 0 0 0 0 0 0
 * 7 0 9 0 0 0 0 3 4
 * 0 0 0 8 4 6 0 2 0
 */

import java.util.Scanner;
import java.lang.Integer;
import java.io.*;

public class Sudoku {
	//Constants
	private static final int NUMBER_OF_ROWS = 9;
	private static final int NUMBER_OF_COLUMNS = NUMBER_OF_ROWS;
	private static final int SUB_GRID_HEIGHT = NUMBER_OF_ROWS / 3;
	private static final int SUB_GRID_WIDTH = NUMBER_OF_COLUMNS / SUB_GRID_HEIGHT;
	private static final int NUMBER_OF_POSSIBLE_VALUES = NUMBER_OF_ROWS;
	private static final char VERTICAL_SEPARATOR = '|';
	private static final char HORIZONTAL_SEPARATOR = '=';
	private static final char BLANK_SYMBOL = '-';
	private static final String COMPLETE_COMMAND = "complete";
	private static final String HINT_COMMAND = "hint";
	private static final String LOAD_COMMAND = "load";
	private static final String SET_COMMAND = "set";
	private static final String QUIT_COMMAND = "quit";
	
	private static final String GOODBYE_MESSAGE = "Goodbye.";
	
	//Main loop variable.
	private static boolean exit;
	
	//Command line fields.
	private static int[][] board;
	private static int rows, columns, cmdRow, cmdCol, cmdNum, subGridWidth, subGridHeight, howManyHSeps;
	private static String[] command;
	private static String defaultPuzzleFileName, cmdFileName, endResponse;
	private static Scanner scan, scanCmd;

	public static void main(String[] args) {
		initialize();
		printWelcomeMessage();
		
		while(!exit) {
			command = scanCmd.nextLine().toLowerCase().split(" ");
			switch (command[0]) {
				case COMPLETE_COMMAND:
					if (complete()) {
						System.out.println("Congratulations! You win!");
						System.out.println("");
						System.out.println("Would you like to play again?");
						endResponse = scanCmd.nextLine().toLowerCase();
						if (endResponse.equals("y") || endResponse.equals("yes")) {
							System.out.println("Use the 'load' command to load a new Sudoku puzzle.");
						} else {
							System.out.println(GOODBYE_MESSAGE);
							exit = true;
						}
					} else {
						System.out.println("Sorry, your Sudoku puzzle is incomplete. Keep trying!");
					}
					break;
				case HINT_COMMAND:
					hint(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
					break;
				case LOAD_COMMAND:
					System.out.println("Loading the file...");
					cmdFileName = command[1];
					load(cmdFileName);
					break;
				case SET_COMMAND:
					try {
						cmdRow = Integer.parseInt(command[1]);
						cmdCol = Integer.parseInt(command[2]);
						cmdNum = Integer.parseInt(command[3]);
						set(cmdRow, cmdCol, cmdNum);
					} catch(Exception exc) {
						System.out.println("You did not enter numbers for the row, column, or number to set. Try again.");
						scanCmd.nextLine();
					}
					break;
				case QUIT_COMMAND:
					System.out.println(GOODBYE_MESSAGE);
					exit = true;
					break;
				default:
					System.out.println("You did not enter a valid command. Try again.");
			}
		}
	}
	
//----------------------------Command Line Methods----------------------------
	
	private static void hint(int r, int c) {
		if (r > 0 && r < NUMBER_OF_ROWS + 1 && c > 0 && c < NUMBER_OF_COLUMNS + 1) {
			boolean[] checkRow = new boolean[10];
			boolean[] checkCol = new boolean[10];
			boolean[] checkSub = new boolean[10];
			
			//Check which numbers are missing from the column.
			for(int col = 0; col < NUMBER_OF_COLUMNS; col++) {
				if (!checkRow[board[r - 1][col]] && board[r - 1][col] != 0) {
					checkRow[board[r - 1][col]] = true;
				}
			}
			
			//Check which numbers are missing from the row.
			for(int row = 0; row < NUMBER_OF_ROWS; row++) {
				if (!checkCol[board[row][c - 1]] && board[row][c - 1] != 0) {
					checkCol[board[row][c - 1]] = true;
				}
			}
			
			//Check which numbers are missing from the sub grid.
			int subStartRow = (r - 1) - ((r - 1) % SUB_GRID_HEIGHT);
			int subStartCol = (c - 1) - ((c - 1) % SUB_GRID_WIDTH);
			for (int begSubR = subStartRow; begSubR < subStartRow + SUB_GRID_HEIGHT; begSubR++ ) {
				for (int begSubC = subStartCol; begSubC < subStartCol + SUB_GRID_WIDTH; begSubC++) {
					if (!checkSub[board[begSubR][begSubC]]) {
						checkSub[board[begSubR][begSubC]] = true;
					}
				}
			}
			//Find which numbers are mutually missing between the column, row, and sub grid.
			StringBuilder validOptions = new StringBuilder();
			for (int index = 1; index <= NUMBER_OF_POSSIBLE_VALUES; index++) {
				if (!checkRow[index] && !checkCol[index] && !checkSub[index]) {
					validOptions.append(index + " ");
				}
			}
			
			//Print out the suggestions.
			System.out.println("Try: " + validOptions);
		} else {
			System.out.println("You did not enter valid numbers for the row, column, and number to place in the corresponding cell. Try again.");
		}
	}
	
	private static void initialize() {
		//Set separation and 'blank' characters and size of the board.
		exit = false;
		howManyHSeps = (NUMBER_OF_COLUMNS * 2 + 3 + (NUMBER_OF_COLUMNS / 2));
		
		defaultPuzzleFileName = "Test.txt";
		board = new int[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];
		scanCmd = new Scanner(System.in);
		load(defaultPuzzleFileName);
	}
	
	//Loads the specified puzzle file into the board.
	private static void load(String cmdFile) {
		try {
			scan = new Scanner(new FileReader(new File(cmdFile)));
			for(int i = 0; i < NUMBER_OF_ROWS; i++) {
				for(int j = 0; j < NUMBER_OF_COLUMNS; j++) {
					board[i][j] = scan.nextInt();
				}
			}
			show();
		} catch(Exception exc) {
			System.out.println("There was an error in attempting to load the specified file. (load command): " + exc.getMessage());
			show();
		}
	}
	
	//Command line set method.
	private static void set(int row, int column, int number) {
		if(row > 0 && row < NUMBER_OF_ROWS + 1 && column > 0 && column < NUMBER_OF_COLUMNS + 1 && number > -1 && number <= NUMBER_OF_POSSIBLE_VALUES) {
			if(valid(row, column, number)) {
				board[row - 1][column - 1] = number;
				show();
			} else {
				System.out.println("The specified number does not belong in that location. Try again.");
			}
		} else {
			System.out.println("You did not enter valid numbers for the row, column, and number to place in the corresponding cell. Try again.");
		}
	}
	
	//Display the board in the command line.
	private static void show() {
		boolean printedRowLine;
		//Iterate over all of the rows and columns to print each value.
		int sudokuValueToken;
		String sudokuValueTextToken;
		for(int i = 0; i < NUMBER_OF_ROWS; i++) {
			printedRowLine = false;
			for(int j = 0; j < NUMBER_OF_COLUMNS; j++) {
				if (i % 3 == 0) {
					if (!printedRowLine) {
						printHorizontalSeparator();
						printedRowLine = true;
					}
				}
				if (j % 3 == 0) {
					System.out.print(VERTICAL_SEPARATOR + " ");
				}
				
				sudokuValueToken = board[i][j];
				sudokuValueTextToken = (sudokuValueToken != 0 ? sudokuValueToken : (BLANK_SYMBOL + "")) + " ";
				if (j == (NUMBER_OF_COLUMNS - 1)) {
					sudokuValueTextToken += VERTICAL_SEPARATOR + "\n";
				}
				System.out.print(sudokuValueTextToken);
			}
		}
		printHorizontalSeparator();
		System.out.println();
	}
	
	private static void printHorizontalSeparator() {
		for(int count = 0; count < howManyHSeps; count++) {
			System.out.print(HORIZONTAL_SEPARATOR);
		}
		System.out.println();
	}
	
//--------------Sudoku Logic Methods--------------

	//This method would check for validity as the user inputs their number.
	private static boolean valid(int r, int c, int n) {
		//r = row, c = column, n = number.
		for(int index = 0; index < NUMBER_OF_ROWS; index++) {
			if(board[r - 1][index] == n || board[index][c - 1] == n) {
				return false;
			}
		}
		int subGridR = (r - 1) - ((r - 1) % SUB_GRID_HEIGHT);
		int subGridC = (c - 1) - ((c - 1) % SUB_GRID_WIDTH);
		int subR = subGridR;
		int subC = subGridC;
		while(subR < subGridR + SUB_GRID_HEIGHT) {
			while(subC < subGridC + SUB_GRID_WIDTH) {
				if(board[subR][subC] == n) {
					return false;
				}
				subC++;
			}
			subR++;
		}
		return true;
	}
	
	//This method checks the board for completion.
	private static boolean complete() {
		if(!blanks() && rows() && columns() && allSubgrids()) {
			return true;
		} else {
			return false;
		}
	}
	
	//Checks the puzzle for any blank spaces.
	private static boolean blanks() {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				if (board[i][j] == 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	//Checks the rows for validity.
	private static boolean rows() {
		boolean[] checkRow = new boolean[NUMBER_OF_ROWS + 1];
		for(int row = 0; row < NUMBER_OF_ROWS; row++) {
			for(int c = 0; c < NUMBER_OF_COLUMNS; c++) {
				if (checkRow[board[row][c]]) {
					return false;
				} else {
					checkRow[board[row][c]] = true;
				}
			}
		}
		return true;
	}
	
	//Checks the columns for validity.
	private static boolean columns() {
		boolean[] checkCol = new boolean[NUMBER_OF_COLUMNS + 1];
		for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
			for(int r = 0; r < NUMBER_OF_ROWS; r++) {
				if (checkCol[board[r][col]]) {
					return false;
				} else {
					checkCol[board[r][col]] = true;
				}
			}
		}
		return true;
	}
	
	//Checks the subgrids for validity.
	private static boolean allSubgrids() {
		for (int i = 0; i < SUB_GRID_HEIGHT; i++) {
			for(int j = 0; j < SUB_GRID_WIDTH; j++) {
				if(!subGrid(i, j)) {
					return false;
				}
			}
		}
		return true;
	}
	
	//Checks one subgrid for validity.
	private static boolean subGrid(int startX, int startY) {
		boolean[] check = new boolean[NUMBER_OF_POSSIBLE_VALUES + 1];
		for (int i = startY; i < SUB_GRID_HEIGHT; i++) {
			for(int j = startX; j < SUB_GRID_WIDTH; j++) {
				if(check[board[i][j]]) {
					return false;
				} else {
					check[board[i][j]] = true;
				}
			}
		}
		return true;
	}
	
	private static void printWelcomeMessage() {
		System.out.println("Welcome to Lavi's Sudoku application.");
		System.out.println("Here is a list of available commands at your disposal:");
		System.out.println("\"load <File name>\" where 'File name' is the full name of the file you'd wish to load into the Sudoku board.");
		System.out.println("\"set <Row number> <Column number> <Number to place at specified location>\"");
		System.out.println("\"hint <Row number> <Column number>\" to receive a hint about the specified entry cell.");
		System.out.println("\"complete\" to check if you've successfully completed the puzzle.");
		System.out.println("\"quit\" to exit the program.\n");
	}
}