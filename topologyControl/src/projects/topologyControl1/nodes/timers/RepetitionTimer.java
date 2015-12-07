package projects.topologyControl1.nodes.timers;

import projects.topologyControl1.nodeDefinitions.BasicNode;
import projects.topologyControl1.nodes.messages.PayloadMsg;
import sinalgo.nodes.timers.Timer;

public class RepetitionTimer extends Timer {
	
	private PayloadMsg msg;
	private Integer interval;
	
	public RepetitionTimer(PayloadMsg msg, Integer interval){
		this.msg = msg;
		this.interval = interval;
	}

	@Override
	public void fire() {
		BasicNode n = (BasicNode) node;
		
		//If the node is dead, don't do anything
		if (n.getIsDead()){
			return;
		}
		
		n.getRouting().sendMessage(msg);
		this.startRelative(interval, n);

	}

}
