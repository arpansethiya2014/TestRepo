package net.codejava.sound;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

/**
 * A Sound Recorder program in Java Swing.
 * @author www.codejava.net
 *
 */
public class SwingSoundRecorder extends JFrame implements ActionListener {

	private JButton buttonRecord = new JButton("Record");
	private JButton buttonPlay = new JButton("Play");
	private JLabel labelRecordTime = new JLabel("Record Time: 00:00:00");

	private SoundRecordingUtil recorder = new SoundRecordingUtil();
	private AudioPlayer player = new AudioPlayer();
	private Thread playbackThread;
	private RecordTimer timer;

	private boolean isRecording = false;
	private boolean isPlaying = false;

	private String saveFilePath;

	// Icons used for buttons
	private ImageIcon iconRecord = new ImageIcon(getClass().getResource(
			"/net/codejava/sound/images/Record.gif"));
	private ImageIcon iconStop = new ImageIcon(getClass().getResource(
			"/net/codejava/sound/images/Stop.gif"));
	private ImageIcon iconPlay = new ImageIcon(getClass().getResource(
			"/net/codejava/sound/images/Play.gif"));

	public SwingSoundRecorder() {
		super("Swing Sound Recorder");
		setLayout(new FlowLayout());

		buttonRecord.setFont(new Font("Sans", Font.BOLD, 14));
		buttonRecord.setIcon(iconRecord);
		buttonPlay.setFont(new Font("Sans", Font.BOLD, 14));
		buttonPlay.setIcon(iconPlay);
		buttonPlay.setEnabled(false);
		labelRecordTime.setFont(new Font("Sans", Font.BOLD, 12));

		add(buttonRecord);
		add(labelRecordTime);
		add(buttonPlay);

		buttonRecord.addActionListener(this);
		buttonPlay.addActionListener(this);

		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	/**
	 * Handle click events on the buttons.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		JButton button = (JButton) event.getSource();
		if (button == buttonRecord) {
			if (!isRecording) {
				startRecording();
			} else {
				stopRecording();
			}

		} else if (button == buttonPlay) {
			if (!isPlaying) {
				playBack();
			} else {
				stopPlaying();
			}
		}
	}

	/**
	 * Start recording sound, the time will count up.
	 */
	private void startRecording() {
		Thread recordThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					isRecording = true;
					buttonRecord.setText("Stop");
					buttonRecord.setIcon(iconStop);
					buttonPlay.setEnabled(false);

					recorder.start();

				} catch (LineUnavailableException ex) {
					JOptionPane.showMessageDialog(SwingSoundRecorder.this,
							"Error", "Could not start recording sound!",
							JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		});
		recordThread.start();
		timer = new RecordTimer(labelRecordTime);
		timer.start();
	}

	/**
	 * Stop recording and save the sound into a WAV file
	 */
	private void stopRecording() {
		isRecording = false;
		try {
			timer.cancel();
			buttonRecord.setText("Record");
			buttonRecord.setIcon(iconRecord);
			
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			recorder.stop();

			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			saveFile();

		} catch (IOException ex) {
			JOptionPane.showMessageDialog(SwingSoundRecorder.this, "Error",
					"Error stopping sound recording!",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	/**
	 * Start playing back the sound.
	 */
	private void playBack() {
		timer = new RecordTimer(labelRecordTime);
		timer.start();
		isPlaying = true;
		playbackThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					buttonPlay.setText("Stop");
					buttonPlay.setIcon(iconStop);
					buttonRecord.setEnabled(false);

					player.play(saveFilePath);
					timer.reset();

					buttonPlay.setText("Play");
					buttonRecord.setEnabled(true);
					buttonPlay.setIcon(iconPlay);
					isPlaying = false;

				} catch (UnsupportedAudioFileException ex) {
					ex.printStackTrace();
				} catch (LineUnavailableException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}
		});

		playbackThread.start();
	}

	/**
	 * Stop playing back.
	 */
	private void stopPlaying() {
		timer.reset();
		timer.interrupt();
		player.stop();
		playbackThread.interrupt();
	}

	/**
	 * Save the recorded sound into a WAV file.
	 */
	private void saveFile() {
		JFileChooser fileChooser = new JFileChooser();
		FileFilter wavFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Sound file (*.WAV)";
			}

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				} else {
					return file.getName().toLowerCase().endsWith(".wav");
				}
			}
		};

		fileChooser.setFileFilter(wavFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int userChoice = fileChooser.showSaveDialog(this);
		if (userChoice == JFileChooser.APPROVE_OPTION) {
			saveFilePath = fileChooser.getSelectedFile().getAbsolutePath();
			if (!saveFilePath.toLowerCase().endsWith(".wav")) {
				saveFilePath += ".wav";
			}

			File wavFile = new File(saveFilePath);

			try {
				recorder.save(wavFile);

				JOptionPane.showMessageDialog(SwingSoundRecorder.this,
						"Saved recorded sound to:\n" + saveFilePath);

				buttonPlay.setEnabled(true);

			} catch (IOException ex) {
				JOptionPane.showMessageDialog(SwingSoundRecorder.this, "Error",
						"Error saving to sound file!",
						JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}

	/**
	 * launch the program
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingSoundRecorder().setVisible(true);
			}
		});
	}

}