package core;

import java.util.Objects;

/**
 * Position class ฉบับปรับปรุงสำหรับ Square Grid View (หน้าจอตรง)
 * ทิศทางเฉียงจะใช้การบวกลบทั้ง Row และ Col พร้อมกันเพื่อให้เดินเฉียง 45 องศาตามสายตา
 */
public class Position {
    private final int row;
    private final int col;

    // นิยามทิศทางตามมาตรฐาน KOMBAT
    public static final int UP = 1;
    public static final int UPRIGHT = 2;
    public static final int DOWNRIGHT = 3;
    public static final int DOWN = 4;
    public static final int DOWNLEFT = 5;
    public static final int UPLEFT = 6;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    /**
     * เมธอด move: ปรับให้เดินเฉียง 45 องศาในทุกทิศทางเฉียง
     * เหมาะสำหรับกรณีที่หน้าจอวาด Column ตรงกันในแนวตั้ง
     */
    public Position move(int direction) {
        int r = this.row;
        int c = this.col;

        switch (direction) {
            case UP: // 1: ขึ้นตรงๆ
                r--;
                break;
            case UPRIGHT: // 2: เฉียงขึ้นขวา
                r--;
                c++;
                break;
            case DOWNRIGHT: // 3: เฉียงลงขวา (เส้นสีฟ้าที่คุณวาด)
                r++;
                c++;
                break;
            case DOWN: // 4: ลงตรงๆ
                r++;
                break;
            case DOWNLEFT: // 5: เฉียงลงซ้าย
                r++;
                c--;
                break;
            case UPLEFT: // 6: เฉียงขึ้นซ้าย
                r--;
                c--;
                break;
        }
        return new Position(r, c);
    }

    /**
     * ตรวจสอบว่าพิกัดอยู่ภายในขอบเขตสนาม 8x8 หรือไม่
     */
    public boolean isValid() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    /**
     * คำนวณระยะห่างแบบ Chebyshev (รวมแนวเฉียงเป็น 1 ก้าว)
     * เพื่อให้การเช็คระยะโจมตีและการซื้อ Hex สอดคล้องกับหน้าจอ
     */
    public int distanceTo(Position other) {
        if (other == null) return Integer.MAX_VALUE;
        return Math.max(Math.abs(this.row - other.row), Math.abs(this.col - other.col));
    }

    /**
     * แปลงจาก Format "row,col"
     */
    public static Position fromString(String s) {
        try {
            String[] parts = s.split(",");
            return new Position(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
        } catch (Exception e) {
            return new Position(0, 0);
        }
    }

    @Override
    public String toString() {
        return row + "," + col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}