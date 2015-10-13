package ca.on.oicr.pde.model;

public class SeqwareAccession implements Accessionable {

    String swid;

    private SeqwareAccession() {
        //
    }

    public SeqwareAccession(String swid) {
        this.swid = swid;
    }

    @Override
    public String getSwid() {
        return swid;
    }
    
    @Override 
    public String toString(){
        return swid;
    }

}