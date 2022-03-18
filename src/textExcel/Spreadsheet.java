package textExcel;
import java.util.ArrayList;

// Spreadsheet: Object Class
// -> Grid: Interface

public class Spreadsheet implements Grid
{
	
	private Cell[][] sheet;
	private int rows = 20;
	private int cols = 12;
	private ArrayList<String> history;
	private boolean haveHistory;
	private int historyLength;
	
	// constructor
	public Spreadsheet() {
		this.sheet = new Cell[getRows()][getCols()];
		for (int i = 0; i < this.sheet.length; i++) {
			for (int j = 0; j < this.sheet[i].length; j++) {
				this.sheet[i][j] = new EmptyCell();
			}
		}
		this.haveHistory = false;
		System.out.println(getGridText());
	}

	// Does a command. Takes String & returns String
	@Override
	public String processCommand(String command)
	{
		if (this.haveHistory == true && !command.contains("history")) {
			this.history.add(0, command);
			if (this.history.size() == this.historyLength + 1) this.history.remove(this.history.size() - 1);
		}
		if (command.length() == 0 || command.equals("")) return "";
		String[] parts = command.split(" ", 3);
		if (command.contains("=")) {
			String cell = parts[0].toUpperCase();
			if (cell.equals("") || cell.equals("=")) return "ERROR: No Specified Cell\n";
			if (cell.charAt(0) > 'L') return "ERROR: Invalid Cell Column\n";
			if (Integer.parseInt(cell.substring(1)) < 1 || Integer.parseInt(cell.substring(1)) > 20) return "ERROR: Invalid Cell Row\n";
			Location loc = new SpreadsheetLocation(cell);
			if (parts.length <= 2 || parts[2].equals("")) return "ERROR: No Value Assigned\n";
			String value = parts[2];
			if (parts[2].startsWith("\"") && parts[2].endsWith("\"")) // Text Cell
				this.sheet[loc.getRow()][loc.getCol()] = new TextCell(value);
			else if (parts[2].endsWith("%")) { // Percent Cell
				for (int i = 0; i < parts[2].length() - 1; i++) {
					if (!"1234567890.-".contains(parts[2].charAt(i)+"")) return "ERROR: Not A Number\n";
				}
				this.sheet[loc.getRow()][loc.getCol()] = new PercentCell(value);
			}
			else if (parts[2].startsWith("(") && parts[2].endsWith(")")) // Formula Cell
				this.sheet[loc.getRow()][loc.getCol()] = new FormulaCell(value, this);
			else if ("1234567890".contains(parts[2].charAt(parts[2].length() - 1)+"")) { // Value Cell
				for (int i = 0; i < parts[2].length(); i++) {
					if (!"1234567890.-".contains(parts[2].charAt(i)+"")) return "ERROR: Not A Number\n";
				}
				this.sheet[loc.getRow()][loc.getCol()] = new ValueCell(value);
			}
			else
				return "ERROR: Assignement Not Possible\n";
			return getGridText();
		}
		if (parts[0].toLowerCase().contains("clear")) {
			if (command.equalsIgnoreCase("clear")) {
				for (int i = 0; i < this.sheet.length; i++) {
					for (int j = 0; j < this.sheet[i].length; j++) {
						this.sheet[i][j] = new EmptyCell();
					}
				}
			} else if (parts.length == 2) {
				String cell = parts[1].toUpperCase();
				if ("1234567890.-".contains(cell.charAt(0)+"") || !"1234567890.-".contains(cell.charAt(1)+"")) return "ERROR: Unclear Clear Command\n";
				if (cell.charAt(0) > 'L') return "ERROR: Invalid Cell Column\n";
				if (Integer.parseInt(cell.substring(1)) < 1 || Integer.parseInt(cell.substring(1)) > 20) return "ERROR: Invalid Cell Row\n";
				Location clearLoc = new SpreadsheetLocation(cell);
				this.sheet[clearLoc.getRow()][clearLoc.getCol()] = new EmptyCell();
			} else {
				return "ERROR: Unclear Clear Command\n";
			}
			return getGridText();
		}
		else if (parts[0].length() <= 3) {
			String cell = parts[0].toUpperCase();
			if ("1234567890.-".contains(cell.charAt(0)+"") || !"1234567890.-".contains(cell.charAt(1)+"")) return "ERROR: Command Not Known\n";
			if (cell.charAt(0) > 'L') return "ERROR: Invalid Cell Column\n";
			if (Integer.parseInt(cell.substring(1)) < 1 || Integer.parseInt(cell.substring(1)) > 20) return "ERROR: Invalid Cell Row\n";
			Location cellLoc = new SpreadsheetLocation(cell);
			return getCell(cellLoc).fullCellText();
		}
		else if (parts[0].equalsIgnoreCase("history")) {
			String commands = "";
			if (parts[1].equalsIgnoreCase("start")) {
				if (this.haveHistory == true) return "ERROR: History Is Already On\n";
				if (parts.length == 2) return "ERROR: No Argument (int)\n";
				if (Integer.parseInt(parts[2]) < 0) return "ERROR: Length Is Negative\n";
				this.historyLength = Integer.parseInt(parts[2]);
				history = new ArrayList<String>();
				this.haveHistory = true;
			}
			else if (parts[1].equalsIgnoreCase("display")) {
				if (this.haveHistory == false) return "ERROR: History Is Off\n";
				for (int i = 0; i < this.history.size(); i++) {
					commands += this.history.get(i) + "\n";
				}
			}
			else if (parts[1].equalsIgnoreCase("clear")) {
				if (this.haveHistory == false) return "ERROR: History Is Off\n";
				if (parts.length == 2) return "ERROR: No Argument (int)\n";
				if (Integer.parseInt(parts[2]) < 0) return "ERROR: Length Is Negative\n";
				int times = Integer.parseInt(parts[2]);
				while (times != 0) {
					this.history.remove(0);
					times--;
				}
			}
			else if (parts[1].equalsIgnoreCase("stop")) {
				if (this.haveHistory == false) return "ERROR: History Is Already Off\n";
				history = null;
				this.haveHistory = false;
			}
			return commands;
		}
		return "ERROR: Command Not Known";
	}

	// Gets the number of rows. Returns int
	@Override
	public int getRows()
	{
		return this.rows;
	}

	// Gets the number of columns. Returns int
	@Override
	public int getCols()
	{
		return this.cols;
	}

	// Gets the cell at a location. Takes Location & returns Cell
	@Override
	public Cell getCell(Location loc)
	{
		return this.sheet[loc.getRow()][loc.getCol()];
	}

	// Gets the value of the grid. Returns String
	@Override
	public String getGridText()
	{
		String text = "   ";
		for (char C = 'A'; C < 'L' + 1; C++) {
			text += "|" + C + "         ";
		}
		text += "|\n";
		for (int i = 0; i < getRows(); i++) {
			if (i + 1 < 10) text += (i + 1) + "  ";
			else text += (i + 1) + " ";
			for (int c = 0; c < getCols(); c++) {
				text += "|" + this.sheet[i][c].abbreviatedCellText();
			}
			text += "|\n";
		}
		return text;
	}

}