import java.util.*;

public class CombinationTest {
    
    public static List<List<Cell>> getPossibleMineLists(List<Cell> freeCells, List<Cell> unknownCells, int minMines, int maxMines) {
        List<List<Cell>> possibleMineLists = new ArrayList<>();
        List<List<Cell>> allCombinations = getAllCombinationsOf(unknownCells);
        for (List<Cell> combination : allCombinations) {
            if (combination.size() <= maxMines && combination.size() >= minMines && combinationIsValidForFreeCells(combination, freeCells)) {
                possibleMineLists.add(combination);
            }
        }
        possibleMineLists.sort(Comparator.comparingInt(List::size));
        return possibleMineLists;
    }

    private static boolean combinationIsValidForFreeCells(List<Cell> combination, List<Cell> freeCells) {
        int[] mineCounts = new int[freeCells.size()];
        for (int i = 0; i < freeCells.size(); i++) {
            Cell freeCell = freeCells.get(i);
            for (Cell unknownCell : combination) {
                if (unknownCell.getEmptyFieldsList().contains(freeCell))
                    mineCounts[i]++;
            }
            if (mineCounts[i] != freeCell.getUnknownMines())
                return false;
        }
        return true;
    }

    private static List<List<Cell>> getAllCombinationsOf(List<Cell> baseList) {
        List<List<Cell>> allCombinations = new ArrayList<>();
        allCombinations.add(List.of());
        for (Cell cell : baseList) {
            List<List<Cell>> tempList = new ArrayList<>(allCombinations);
            for (List<Cell> combination : tempList) {
                List<Cell> newCombination = new ArrayList<>(combination);
                newCombination.add(cell);
                allCombinations.add(newCombination);
            }
        }
        return allCombinations;
    }

    public static List<Cell> getSureMines(List<List<Cell>> possibleMineLists, List<Cell> reachableUnknowns) {
        List<Cell> sureMines = new ArrayList<>();
        for (Cell cell : reachableUnknowns) {
            if (isInAllLists(cell, possibleMineLists))
                sureMines.add(cell);
        }
        return sureMines;
    }

    private static boolean isInAllLists(Cell cell, List<List<Cell>> mineLists) {
        for (List<Cell> combination : mineLists) {
            if (!combination.contains(cell)) {
                return false;
            }
        }
        return true;
    }

    public static List<Cell> getSureFree(List<List<Cell>> possibleMineLists, List<Cell> unknownCells) {
        List<Cell> sureFree = new ArrayList<>(unknownCells);
        for (List<Cell> combination : possibleMineLists) {
            sureFree.removeAll(combination);
        }
        return sureFree;
    }

    public static List<Cell> getSingleListOfSize(int remainingMines, List<List<Cell>> possibleMineLists) {
        List<List<Cell>> mineListsWithRightLength = possibleMineLists.stream()
                .filter(list -> list.size() == remainingMines)
                .toList();
        if (mineListsWithRightLength.size() == 1)
            return mineListsWithRightLength.get(0);
        return null;
    }
}
