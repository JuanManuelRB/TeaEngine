package engine.graphic;

public class ShaderError extends Error {
    public ShaderError(){
        super();
    }
    public ShaderError(String message){
        super(message);
    }

    public ShaderError(String message, Throwable cause) {
        super(message, cause);
    }

    public ShaderError(Throwable cause) {
        super(cause);
    }
}
