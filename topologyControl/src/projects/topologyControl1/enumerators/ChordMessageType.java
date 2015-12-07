package projects.topologyControl1.enumerators;

public enum ChordMessageType {
	// a partir do numero 101
	ANSWER_MONITOR_ID(101),//informa � baseStation que o no � monitor
	SEND_TO_SUPERVISOR(102),//informa que as mensagens estao sendo enviadas ao supervisor(signature)
	NOTIFY_NEIGHBORS(103),//notifica os vizinhos do monitor que ele � supervisor
	MONITOR_DOWN(104);//notifica � baseStation que um supervisor morreu ou est� fora da rede
	
	private Integer value;
	
	private ChordMessageType(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}
	
	public static Integer getMaxValue(){
		return ChordMessageType.values()[0].getValue();
	}
	
	public static Integer getMinValue(){
		return ChordMessageType.values()[ChordMessageType.values().length - 1].getValue();
	}
}
