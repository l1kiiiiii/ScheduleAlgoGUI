import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

// Abstract class for Scheduling
abstract class SchedulingAlgorithm {
    protected int[] burstTime;
    protected int[] waitingTime;
    protected int[] turnaroundTime;
    protected int n;
    protected ArrayList<String> ganttChart = new ArrayList<>();

    public SchedulingAlgorithm(int[] burstTime, int n) {
        this.burstTime = burstTime;
        this.n = n;
        this.waitingTime = new int[n];
        this.turnaroundTime = new int[n];
    }

    // Abstract method to calculate scheduling times
    public abstract void calculateTimes();

    // Method to display the results
    public String displayTimes() {
        int totalWaitingTime = 0, totalTurnaroundTime = 0;
        StringBuilder result = new StringBuilder("Process\tBurst Time\tWaiting Time\tTurnaround Time\n");
        for (int i = 0; i < n; i++) {
            result.append("P").append(i + 1).append("\t\t").append(burstTime[i]).append("\t\t")
                    .append(waitingTime[i]).append("\t\t").append(turnaroundTime[i]).append("\n");
            totalWaitingTime += waitingTime[i];
            totalTurnaroundTime += turnaroundTime[i];
        }
        result.append("\nAverage Waiting Time = ").append((float) totalWaitingTime / n).append("\n");
        result.append("Average Turnaround Time = ").append((float) totalTurnaroundTime / n).append("\n");
        return result.toString();
    }

    public ArrayList<String> getGanttChart() {
        return ganttChart;
    }
}

// FCFS Scheduling
class FCFS extends SchedulingAlgorithm {
    public FCFS(int[] burstTime, int n) {
        super(burstTime, n);
    }

    @Override
    public void calculateTimes() {
        waitingTime[0] = 0;
        ganttChart.clear();
        for (int i = 0; i < n; i++) {
            ganttChart.add("P" + (i + 1));
            if (i > 0) {
                waitingTime[i] = burstTime[i - 1] + waitingTime[i - 1];
            }
            turnaroundTime[i] = burstTime[i] + waitingTime[i];
        }
    }
}

// SJF Scheduling
class SJF extends SchedulingAlgorithm {
    public SJF(int[] burstTime, int n) {
        super(burstTime, n);
    }

    @Override
    public void calculateTimes() {
        int[] burstTimeCopy = Arrays.copyOf(burstTime, n);
        int[] processIndex = new int[n];
        for (int i = 0; i < n; i++) {
            processIndex[i] = i;
        }

        // Sort burst times and process indices together
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (burstTimeCopy[j] > burstTimeCopy[j + 1]) {
                    int temp = burstTimeCopy[j];
                    burstTimeCopy[j] = burstTimeCopy[j + 1];
                    burstTimeCopy[j + 1] = temp;

                    int tempIdx = processIndex[j];
                    processIndex[j] = processIndex[j + 1];
                    processIndex[j + 1] = tempIdx;
                }
            }
        }

        ganttChart.clear();
        waitingTime[processIndex[0]] = 0;
        ganttChart.add("P" + (processIndex[0] + 1));
        for (int i = 1; i < n; i++) {
            waitingTime[processIndex[i]] = burstTimeCopy[i - 1] + waitingTime[processIndex[i - 1]];
            ganttChart.add("P" + (processIndex[i] + 1));
        }
        for (int i = 0; i < n; i++) {
            turnaroundTime[processIndex[i]] = burstTime[processIndex[i]] + waitingTime[processIndex[i]];
        }
    }
}

// Round Robin Scheduling
class RoundRobin extends SchedulingAlgorithm {
    private int quantum;

    public RoundRobin(int[] burstTime, int n, int quantum) {
        super(burstTime, n);
        this.quantum = quantum;
    }

    @Override
    public void calculateTimes() {
        int[] remainingBurstTime = Arrays.copyOf(burstTime, n);
        int time = 0;
        boolean done;

        ganttChart.clear();
        do {
            done = true;
            for (int i = 0; i < n; i++) {
                if (remainingBurstTime[i] > 0) {
                    done = false;
                    ganttChart.add("P" + (i + 1));
                    if (remainingBurstTime[i] > quantum) {
                        time += quantum;
                        remainingBurstTime[i] -= quantum;
                    } else {
                        time += remainingBurstTime[i];
                        waitingTime[i] = time - burstTime[i];
                        remainingBurstTime[i] = 0;
                    }
                }
            }
        } while (!done);

        for (int i = 0; i < n; i++) {
            turnaroundTime[i] = burstTime[i] + waitingTime[i];
        }
    }
}
// gui
class OSSchedulingSimulatorGUI extends JFrame {
    private JTextField processField;
    private JTextArea burstTimesArea;
    private JTextArea resultArea;
    private JComboBox<String> algorithmBox;
    private JTextField quantumField;
    private JPanel ganttPanel;

    public OSSchedulingSimulatorGUI() {
        setTitle("OS Scheduling Simulator");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Number of Processes:"));
        processField = new JTextField();
        inputPanel.add(processField);

        inputPanel.add(new JLabel("Burst Times (comma separated):"));
        burstTimesArea = new JTextArea();
        inputPanel.add(burstTimesArea);

        inputPanel.add(new JLabel("Select Algorithm:"));
        algorithmBox = new JComboBox<>(new String[]{"FCFS", "SJF", "Round Robin"});
        inputPanel.add(algorithmBox);

        inputPanel.add(new JLabel("Time Quantum (for RR):"));
        quantumField = new JTextField();
        inputPanel.add(quantumField);

        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(new CalculateButtonListener());
        inputPanel.add(calculateButton);

        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        ganttPanel = new JPanel();
        ganttPanel.setBackground(Color.WHITE);
        add(ganttPanel, BorderLayout.SOUTH);
    }


    private void drawGanttChart(ArrayList<String> ganttChart) {
        ganttPanel.removeAll();
        ganttPanel.setLayout(new BorderLayout());

        JPanel chartPanel = new JPanel(new GridLayout(1, ganttChart.size()));
        JPanel timelinePanel = new JPanel(new GridLayout(1, ganttChart.size() + 1));

        int time = 0;
        JLabel timeLabel = new JLabel(String.valueOf(time), SwingConstants.CENTER);
        timelinePanel.add(timeLabel);

        for (String process : ganttChart) {
            JLabel label = new JLabel(process, SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            chartPanel.add(label);

            // Calculate and add the next time value
            int burstTime = Integer.parseInt(process.replace("P", "")) - 1; // Get process index
            time += burstTime;
            JLabel timelineLabel = new JLabel(String.valueOf(time), SwingConstants.CENTER);
            timelinePanel.add(timelineLabel);
        }

        ganttPanel.add(chartPanel, BorderLayout.CENTER);
        ganttPanel.add(timelinePanel, BorderLayout.SOUTH);

        ganttPanel.revalidate();
        ganttPanel.repaint();
    }


    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int n = Integer.parseInt(processField.getText().trim());
                String[] burstTimeStrings = burstTimesArea.getText().trim().split(",");
                int[] burstTimes = Arrays.stream(burstTimeStrings).mapToInt(Integer::parseInt).toArray();

                if (burstTimes.length != n) {
                    JOptionPane.showMessageDialog(null, "Number of burst times must match number of processes.");
                    return;
                }

                SchedulingAlgorithm scheduler = null;
                String selectedAlgorithm = (String) algorithmBox.getSelectedItem();

                switch (selectedAlgorithm) {
                    case "FCFS":
                        scheduler = new FCFS(burstTimes, n);
                        break;
                    case "SJF":
                        scheduler = new SJF(burstTimes, n);
                        break;
                    case "Round Robin":
                        int quantum = Integer.parseInt(quantumField.getText().trim());
                        scheduler = new RoundRobin(burstTimes, n, quantum);
                        break;
                }

                if (scheduler != null) {
                    scheduler.calculateTimes();
                    resultArea.setText(scheduler.displayTimes());
                    drawGanttChart(scheduler.getGanttChart());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OSSchedulingSimulatorGUI simulator = new OSSchedulingSimulatorGUI();
            simulator.setVisible(true);
        });
    }
}
