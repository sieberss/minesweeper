import java.util.*;

public record SolutionSuggestion(
        int mineNumber,
        List<Cell> sureMines
) {

    public static SolutionSuggestion suggestMinimalMineSolution(List<Cell> uncompletedFree, List<Cell> unknownsWithSeveralFreeNeighbors) {
        Map<Cell, Integer> missingMines = new HashMap<>();
        for (Cell cell : uncompletedFree) {
            missingMines.put(cell, cell.getUnknownMines());
        }
        List<Cell> sureMines = new ArrayList<>();
        // add as mine first those unknowns that fit to most free cells
        unknownsWithSeveralFreeNeighbors.sort(Comparator.comparing(cell -> -cell.getEmptyFieldsList().size()));
        for (Cell cell : unknownsWithSeveralFreeNeighbors) {
            List<Cell> freeNeighbors = cell.getEmptyFieldsList();
            if (freeNeighbors.stream().allMatch(neighbor -> missingMines.get(neighbor) > 0)) {
                freeNeighbors.forEach(neighbor -> missingMines.put(neighbor, missingMines.get(neighbor) - 1));
                sureMines.add(cell);
            }
        }
        int stillMissing = missingMines.values().stream().mapToInt(Integer::intValue).sum();
        return new SolutionSuggestion(stillMissing + sureMines.size(), sureMines);
    }


}

