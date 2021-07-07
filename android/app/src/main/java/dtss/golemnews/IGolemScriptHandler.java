package dtss.golemnews;

public interface IGolemScriptHandler {

    void dataReceived(GolemScript.GolemScriptDataType type, Object data);
    void cacheLoaded(GolemScript sender);


}
