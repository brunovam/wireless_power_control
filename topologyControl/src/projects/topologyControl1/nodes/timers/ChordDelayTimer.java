package projects.topologyControl1.nodes.timers;

import projects.topologyControl1.enumerators.ChordMessageType;
import projects.topologyControl1.nodeDefinitions.BasicNode;
import projects.topologyControl1.nodeDefinitions.chord.UtilsChord;
import projects.topologyControl1.nodes.nodeImplementations.MonitorNode;
import sinalgo.nodes.timers.Timer;

/**
 * A message sent to baseStation to advise there is a supervisor dead or out of the network
 * @author Alex Lacerda Ramos
 *
 */
public class ChordDelayTimer extends Timer{

	/**
	 * check whether there is any monitor down in the monitor list of a neighbor  
	 */
	@Override
	public void fire() {
		if (!(node instanceof BasicNode)) {
			return;
		}
	
		BasicNode basicNode = (BasicNode) node;
		
		for (MonitorNode monitor : basicNode.monitors) {
			if (monitor.getIsDead()) {
				basicNode.sendMessageToBaseStation(ChordMessageType.MONITOR_DOWN.getValue());
				UtilsChord.removeMonitorFromLists(monitor);//remove os monitores deads das listas de seus vizinhos
				UtilsChord.removeTimers();//no caso de nós que só tinham este monitor como vizinho, é preciso remover seus timer
											// que checam se um monitor vizinho está dead
			}
		}
		
		this.startRelative(BasicNode.DELAY_TIME, basicNode);//recursive repetition on every time interval
	}
}
