package projects.topologyControl1.nodes.timers;

import projects.topologyControl1.nodeDefinitions.BasicNode;
import sinalgo.nodes.timers.Timer;

public class RepeatSendMessageTimer extends Timer {
	private Integer interval;
	
	public RepeatSendMessageTimer(Integer interval){
		this.interval = interval;		
	}

	@Override
	public void fire() {
		BasicNode n = (BasicNode)node;
		n.getRouting().sendMessage(10);
		this.startRelative(interval, n);		
	}

}
