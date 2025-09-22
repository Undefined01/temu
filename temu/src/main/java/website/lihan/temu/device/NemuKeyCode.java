package website.lihan.temu.device;

public enum NemuKeyCode {
  NONE(0),
  ESCAPE(1),
  F1(2),
  F2(3),
  F3(4),
  F4(5),
  F5(6),
  F6(7),
  F7(8),
  F8(9),
  F9(10),
  F10(11),
  F11(12),
  F12(13),
  GRAVE(14),
  DIGIT1(15),
  DIGIT2(16),
  DIGIT3(17),
  DIGIT4(18),
  DIGIT5(19),
  DIGIT6(20),
  DIGIT7(21),
  DIGIT8(22),
  DIGIT9(23),
  DIGIT0(24),
  MINUS(25),
  EQUALS(26),
  BACKSPACE(27),
  TAB(28),
  Q(29),
  W(30),
  E(31),
  R(32),
  T(33),
  Y(34),
  U(35),
  I(36),
  O(37),
  P(38),
  LEFTBRACKET(39),
  RIGHTBRACKET(40),
  BACKSLASH(41),
  CAPSLOCK(42),
  A(43),
  S(44),
  D(45),
  F(46),
  G(47),
  H(48),
  J(49),
  K(50),
  L(51),
  SEMICOLON(52),
  APOSTROPHE(53),
  RETURN(54),
  LSHIFT(55),
  Z(56),
  X(57),
  C(58),
  V(59),
  B(60),
  N(61),
  M(62),
  COMMA(63),
  PERIOD(64),
  SLASH(65),
  RSHIFT(66),
  LCTRL(67),
  APPLICATION(68),
  LALT(69),
  SPACE(70),
  RALT(71),
  RCTRL(72),
  UP(73),
  DOWN(74),
  LEFT(75),
  RIGHT(76),
  INSERT(77),
  DELETE(78),
  HOME(79),
  END(80),
  PAGEUP(81),
  PAGEDOWN(82),
  ;

  public final int code;

  NemuKeyCode(int code) {
    this.code = code;
  }

  public static NemuKeyCode fromJavaFXKeyCode(javafx.scene.input.KeyCode keyCode) {
    return switch (keyCode) {
      case ESCAPE -> ESCAPE;
      case F1 -> F1;
      case F2 -> F2;
      case F3 -> F3;
      case F4 -> F4;
      case F5 -> F5;
      case F6 -> F6;
      case F7 -> F7;
      case F8 -> F8;
      case F9 -> F9;
      case F10 -> F10;
      case F11 -> F11;
      case F12 -> F12;
      case BACK_QUOTE -> GRAVE;
      case DIGIT1 -> DIGIT1;
      case DIGIT2 -> DIGIT2;
      case DIGIT3 -> DIGIT3;
      case DIGIT4 -> DIGIT4;
      case DIGIT5 -> DIGIT5;
      case DIGIT6 -> DIGIT6;
      case DIGIT7 -> DIGIT7;
      case DIGIT8 -> DIGIT8;
      case DIGIT9 -> DIGIT9;
      case DIGIT0 -> DIGIT0;
      case MINUS -> MINUS;
      case EQUALS -> EQUALS;
      case BACK_SPACE -> BACKSPACE;
      case TAB -> TAB;
      case Q -> Q;
      case W -> W;
      case E -> E;
      case R -> R;
      case T -> T;
      case Y -> Y;
      case U -> U;
      case I -> I;
      case O -> O;
      case P -> P;
      case OPEN_BRACKET -> LEFTBRACKET;
      case CLOSE_BRACKET -> RIGHTBRACKET;
      case BACK_SLASH -> BACKSLASH;
      case CAPS -> CAPSLOCK;
      case A -> A;
      case S -> S;
      case D -> D;
      case F -> F;
      case G -> G;
      case H -> H;
      case J -> J;
      case K -> K;
      case L -> L;
      case SEMICOLON -> SEMICOLON;
      case QUOTE -> APOSTROPHE;
      case ENTER -> RETURN;
      case SHIFT -> LSHIFT;
      case Z -> Z;
      case X -> X;
      case C -> C;
      case V -> V;
      case B -> B;
      case N -> N;
      case M -> M;
      case COMMA -> COMMA;
      case PERIOD -> PERIOD;
      case SLASH -> SLASH;
      case CONTROL -> LCTRL;
      case ALT -> LALT;
      case SPACE -> SPACE;
      case UP -> UP;
      case DOWN -> DOWN;
      case LEFT -> LEFT;
      case RIGHT -> RIGHT;
      case INSERT -> INSERT;
      case DELETE -> DELETE;
      case HOME -> HOME;
      case END -> END;
      case PAGE_UP -> PAGEUP;
      case PAGE_DOWN -> PAGEDOWN;
      default -> NONE;
    };
  }
}
