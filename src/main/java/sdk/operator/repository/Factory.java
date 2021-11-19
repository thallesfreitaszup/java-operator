package sdk.operator.repository;

public class Factory {

    public static Repository newRepository(RepositoryType type) {
            switch (type){
                case GITHUB:
                    return new Github();
                default:
                    throw new IllegalArgumentException("Not yet implemented");
            }
    }
}
