package Ctransformers.encode;

public interface IEncoder {
    public int encode(String uri);
    public void saveMappings(String filename);

}
