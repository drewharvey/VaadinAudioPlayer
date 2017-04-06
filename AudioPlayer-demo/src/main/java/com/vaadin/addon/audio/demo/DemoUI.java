package com.vaadin.addon.audio.demo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.audio.server.AudioPlayer;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Slider;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

@Theme("demo")
@Title("AudioPlayer Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{
    public static class Controls extends Panel {
    	
    	private AudioPlayer player;
		
    	private Slider positionSlider;
    	private Slider volumeSlider;
    	private Slider balanceSlider;
    	private Slider speedSlider;
    	
    	private Button rewButton;
    	private Button stopButton;
    	private Button pauseButton;
    	private Button playButton;
    	private Button fwdButton;
    	
    	public Controls(AudioPlayer player, String streamName) {
    		super("Stream " + streamName);
    		VerticalLayout layout = new VerticalLayout();
    		layout.setSpacing(true);
    		layout.setMargin(true);
    		layout.setSizeFull();
    		
    		this.player = player;
    		
    		positionSlider = new Slider("Position");
    		positionSlider.setSizeFull();
    		// TODO: connect position slider to player
    		
    		layout.addComponent(positionSlider);

    		VerticalLayout innerContainer = new VerticalLayout();
    		innerContainer.setWidth("100%");
    		
    		HorizontalLayout buttonLayout = new HorizontalLayout();
    		buttonLayout.setSpacing(true);

    		buttonLayout.addComponent(
	    		rewButton = new Button("<<", new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						player.skip(-5000);
					}
				})
    		);

    		buttonLayout.addComponent(
	    		stopButton = new Button("|~|", new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						player.stop();
					}
	    		})
    		);
    		
    		buttonLayout.addComponent(
				pauseButton = new Button("||", new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if(player.isPaused()) {
							player.resume();
						} else {
							player.pause();
						}
					}
				})
			);

    		buttonLayout.addComponent(
	    		playButton = new Button("|>", new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if(player.isStopped()) {
							player.play();
						} else if(player.isPaused()) {
							player.resume();
						} else {
							player.stop();
							player.play(0);
						}
					}
				})
    		);
    		
    		buttonLayout.addComponent(
	    		fwdButton = new Button(">>", new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						player.skip(5000);
					}
	    		})
    		);
    		
    		innerContainer.addComponent(buttonLayout);
    		innerContainer.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
    		
    		HorizontalLayout sliderLayout = new HorizontalLayout();
    		sliderLayout.setSpacing(true);
    		
    		sliderLayout.addComponent(
    			volumeSlider = new Slider("Volume")
			);
    		volumeSlider.setMin(0);
    		volumeSlider.setMax(100);
    		volumeSlider.setValue(80d);
    		volumeSlider.setWidth("150px");
    		volumeSlider.addValueChangeListener(e -> {
    			final double volume = volumeSlider.getValue();
    			player.setVolume(volume);
    		});
    		
    		sliderLayout.addComponent(
    			balanceSlider = new Slider("Balance")
			);
    		balanceSlider.setWidth("150px");
    		balanceSlider.setMin(-1);
    		balanceSlider.setMax(1);
    		balanceSlider.setValue(0d);
    		balanceSlider.addValueChangeListener(e -> {
    			final double balance = balanceSlider.getValue();
    			player.setBalance(balance);
    		});

    		
    		sliderLayout.addComponent(
    			speedSlider = new Slider("Speed")
			);
    		speedSlider.setWidth("150px");
    		speedSlider.setMin(-4);
    		speedSlider.setMax(4);
    		speedSlider.setValue(0d);
    		speedSlider.addValueChangeListener(e -> {
    			final double playbackSpeed = speedSlider.getValue();
    			player.setPlaybackSpeed(playbackSpeed);
    		});
    		
    		innerContainer.addComponent(sliderLayout);
    		innerContainer.setComponentAlignment(sliderLayout, Alignment.MIDDLE_CENTER);
    		
    		layout.addComponent(innerContainer);
    		setContent(layout);
    	}
    	
    	public AudioPlayer getPlayer() {
			return player;
		}

		public Slider getPositionSlider() {
			return positionSlider;
		}

		public Button getRewButton() {
			return rewButton;
		}

		public Button getStopButton() {
			return stopButton;
		}

		public Button getPauseButton() {
			return pauseButton;
		}

		public Button getPlayButton() {
			return playButton;
		}

		public Button getFwdButton() {
			return fwdButton;
		}
    }
    
	public static class FileSelector extends Panel {
    	
		public static abstract class SelectionCallback {
			public abstract void onSelected(String itemName);
		}
		
		private Set<SelectionCallback> callbacks;
		private ComboBox fileList;
    	private Button addButton;

    	public FileSelector() {
    		
    		super("File selector");
    		
    		callbacks = new HashSet<SelectionCallback>();
    		HorizontalLayout wrapper = new HorizontalLayout();
    		HorizontalLayout layout = new HorizontalLayout();
    		layout.setSpacing(true);
    		layout.setMargin(true);
    		
    		layout.addComponent(fileList = new ComboBox("File", Arrays.asList(new String[] {
    			"test1.wav",
    			"test2.wav",
    			"test3.wav",
    			"none of these exist"
    		})));
    		
    		layout.addComponent(addButton = new Button("Add stream", new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					String fileName = (String) fileList.getValue();
					if(fileName != null && !fileName.equals("")) {
						for(SelectionCallback cb : callbacks) {
							cb.onSelected(fileName);
						}
						System.out.println("add stream " + fileName);
					} else {
						System.out.println("no file selected, cannot add stream");
					}
				}
    		}));
    		layout.setComponentAlignment(addButton, Alignment.BOTTOM_CENTER);
    		
    		wrapper.addComponent(layout);
    		wrapper.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
    		setContent(wrapper);
    	}
    	
    	public FileSelector(SelectionCallback cb) {
    		this();
    		callbacks.add(cb);
    	}

    	public ComboBox getFileList() {
			return fileList;
		}

		public Button getAddButton() {
			return addButton;
		}

    }
    
    @Override
    protected void init(VaadinRequest request) {

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
    	
    	final FileSelector fileSelector = new FileSelector(new FileSelector.SelectionCallback() {
			@Override
			public void onSelected(String itemName) {
		        // TODO: create and attach a stream to the audio player...
				AudioPlayer audio = new AudioPlayer(null);
		        Controls controls = new Controls(audio, itemName);
		        layout.addComponent(controls);
			}
		});

        layout.addComponent(fileSelector);
        setContent(layout);
    }
    
	//=========================================================================
	//=========================================================================
	//=========================================================================
    
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {}
}
