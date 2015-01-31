package engine;

public class LetterABuilder extends MeshBuilder {


    private double height;

    private LetterABuilder() {
    }

    public static LetterABuilder aLetterA() {
        return new LetterABuilder();
    }

    public LetterABuilder withHeight(double height) {
        this.height = height;
        return this;
    }

    public Mesh build() {

        double scale = height / 10;
        double t = 2;
        double b = 1.5;
        double k = 4.0 / 10;

        return new Mesh(
                vertices(
                        vertex(-5 , -5 , -1 ),
                        vertex(-1 , 5 , -1 ),
                        vertex(1 , 5 , -1 ),
                        vertex(5 , -5 , -1 ),
                        vertex((5 - t) , -5 , -1 ),
                        vertex((5 - t - b * k) , -(5 - b) , -1 ),
                        vertex((5 - t - (b + t) * k) , -(5 - b - t) , -1 ),
                        vertex(0, (5 - 1 / k) , -1 ),
                        vertex(-(5 - t - (b + t) * k) , -(5 - b - t) , -1 ),
                        vertex(-(5 - t - b * k) , -(5 - b) , -1 ),
                        vertex(-(5 - t) , -5 , -1 ),

                        vertex(-5 , -5 , 1 ),
                        vertex(-1 , 5 , 1 ),
                        vertex(1 , 5 , 1 ),
                        vertex(5 , -5 , 1 ),
                        vertex((5 - t) , -5 , 1 ),
                        vertex((5 - t - b * k) , -(5 - b) , 1 ),
                        vertex((5 - t - (b + t) * k) , -(5 - b - t) , 1 ),
                        vertex(0, (5 - 1 / k) , 1 ),
                        vertex(-(5 - t - (b + t) * k) , -(5 - b - t) , 1 ),
                        vertex(-(5 - t - b * k) , -(5 - b) , 1 ),
                        vertex(-(5 - t) , -5 , 1 )

                ),
                faces(
                        face(0, 1, 7, 8, 6, 7, 1, 2, 3, 4, 5, 9, 10),
                        face(21, 20, 16, 15, 14, 13, 12, 18, 17, 19, 18, 12, 11),
                        face(0,10,21,11),
                        face(11,12,1,0),
                        face(1,12,13,2),
                        face(19,17,6,8),
                        face(19,8,7,18),
                        face(17,18,7,6),
                        face(21,10,9,20),
                        face(20,9,5,16),
                        face(15,16,5,4),
                        face(15,4,3,14),
                        face(14,3,2,13)

                )
        ).scale(scale);
    }

}
