package com.github.mrmks.mc.template;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PrimitiveIterator;
import java.util.Stack;

public class FormulaAPI {

    public static Number mathCal(String s) {
        if (s == null || s.isEmpty()) return null;
        s = s.trim();
        PrimitiveIterator.OfInt it = s.codePoints().iterator();
        int v;

        LinkedList<MathThing> ms = new LinkedList<>();
        Stack<Integer> bs = new Stack<>();

        int ts = -1;
        while (it.hasNext() || ts >= 0) {
            v = ts < 0 ? it.next() : ts;
            ts = -1;
            if (v >= Character.MIN_SUPPLEMENTARY_CODE_POINT) return null;
            if (v <= 32) continue;

            if (v >= '0' && v <= '9') {
                int d = -1, n = v - 48, fn = 1;
                while (it.hasNext()) {
                    v = it.next();
                    if (v >= '0' && v <= '9') {
                        if (d < 0) n = n * 10 + v - 48;
                        else {
                            fn = fn * 10 + v - 48;
                            d++;
                        }
                    } else if (v == '.') {
                        if (d < 0) d = 0;
                        else return null;
                    } else {
                        ts = v;
                        break;
                    }
                }
                double r = n;
                if (d > 0) {
                    double fc = 0.1;
                    while (--d > 0) fc *= 0.1;
                    r += (fc * fn - 1);
                }
                if (!chkFst(ms)) return null;
                ms.add(new Num(r));
            } else if ((v >= 'A' && v <= 'Z') || (v >= 'a' && v <= 'z')) {
                char cv = (char) (v <= 'Z' ? v + 32 : v);
                StringBuilder bd = new StringBuilder();
                bd.append(cv);
                while (it.hasNext()) {
                    v = it.next();
                    if (v >= 'A' && v <= 'Z' || v >= 'a' && v <= 'z') {
                        cv = (char) (v <= 'Z' ? v + 32 : v);
                        bd.append(cv);
                    } else {
                        String str = bd.toString();
                        if (!chkFst(ms)) return null;
                        switch (str) {
                            case "round":
                                ms.add(new FuncRound());
                                break;
                            case "floor":
                                ms.add(new FuncFloor());
                                break;
                            case "ceil":
                                ms.add(new FuncCeil());
                                break;
                            default:
                                return null;
                        }
                        ts = v;
                        break;
                    }
                }
            } else {
                switch (v) {
                    case '+':
                        if (!chkSn(ms)) return null;
                        ms.add(OperateSymbol.ADD);
                        break;
                    case '-':
                        if (!chkSn(ms)) return null;
                        ms.add(OperateSymbol.SUBTRACT);
                        break;
                    case '*':
                        if (!chkLast(ms)) return null;
                        ms.add(OperateSymbol.MULTIPLIER);
                        break;
                    case '/':
                        if (!chkLast(ms)) return null;
                        ms.add(OperateSymbol.DIVIDE);
                        break;
                    case '\\':
                        if (!chkLast(ms)) return null;
                        ms.add(OperateSymbol.MOD);
                        break;
                    case '^':
                        if (!chkLast(ms)) return null;
                        ms.add(OperateSymbol.POW);
                        break;
                    case '(':
                        if (!chkGpFst(ms)) return null;
                        bs.add(ms.size());
                        ms.add(NonFunc.GROUP_START);
                        break;
                    case ')':
                        if (!chkLast(ms) || bs.size() < 1) return null;
                        int bi = bs.pop();
                        LinkedList<MathThing> tl = new LinkedList<>();
                        while (ms.size() > bi) tl.offerFirst(ms.removeLast());
                        tl.removeFirst();
                        if (ms.getLast() instanceof FuncSym) {
                            ((FuncSym) ms.getLast()).l = tl;
                        } else {
                            ms.add(new Group(tl));
                        }
                        break;
                    case ',':
                        if (!chkLast(ms)) return null;
                        ms.add(NonFunc.COMMA);
                        break;
                    default:
                        return null;
                }
            }
        }

        if (bs.size() > 0) return null;

        if (ms.size() == 0) return null;
        if (!chkLast(ms)) return null;

        return calListRecursion(ms);
    }

    private static Double calListRecursion(LinkedList<MathThing> list) {
        if (list.isEmpty()) return null;
        if (list.getFirst() == OperateSymbol.ADD || list.getFirst() == OperateSymbol.SUBTRACT) list.removeFirst();

        if (list.size() == 1 && list.getFirst() instanceof MathResult) return ((MathResult) list.getFirst()).cal();

        LinkedList<OperateSymbol> ops = new LinkedList<>();
        LinkedList<Num> nums = new LinkedList<>();
        int mp = Integer.MIN_VALUE;

        ListIterator<MathThing> lit = list.listIterator(list.size());
        while (lit.hasPrevious() && lit.previousIndex() > 1) {
            MathThing mt = lit.previous();
            if (mt instanceof MathResult) {
                Num nm;
                if (mt instanceof Num) nm = (Num) mt;
                else {
                    Double d = ((MathResult) mt).cal();
                    if (d == null) return null;
                    nm = new Num(d);
                    lit.set(nm);
                }
                nums.push(nm);
                MathThing mt2 = lit.previous();
                MathThing mt1 = lit.previous();
                lit.next();
                OperateSymbol op;
                if (mt2 instanceof OperateSymbol) {
                    op = (OperateSymbol) mt2;
                    if (mt1 instanceof OperateSymbol) {
                        if (op == OperateSymbol.SUBTRACT) {
                            nm.n = -nm.n;
                        } else if (op != OperateSymbol.ADD) return null;
                        lit.next();
                        lit.remove();
                        op = (OperateSymbol) mt1;
                    }
                } else return null;
                ops.push(op);
                mp = Math.max(mp, op.priority());
            } else return null;
        }
        if (list.getFirst() instanceof MathResult) {
            if (list.getFirst() instanceof Num) nums.push((Num) list.getFirst());
            else nums.push(new Num(((MathResult) list.getFirst()).cal()));
        } else return null;

        int np = Integer.MIN_VALUE;
        for (int i = mp; !ops.isEmpty(); i = np) {
            ListIterator<OperateSymbol> it = ops.listIterator();
            while (it.hasNext()) {
                OperateSymbol op = it.next();
                if (op.priority() == i) {
                    Num a = nums.get(it.previousIndex());
                    a.n = operate(a.n, nums.remove(it.previousIndex() + 1).n, op);
                    it.remove();
                } else {
                    np = Math.max(op.priority(), np);
                }
            }
        }

        return nums.size() == 1 ? nums.poll().n : null;
    }

    private static double operate(double a, double b, OperateSymbol op) {
        switch (op) {
            case POW:
                return Math.pow(a, b);
            case MULTIPLIER:
                return a * b;
            case DIVIDE:
                return a / b;
            case MOD:
                return (long) a % (long) b;
            case ADD:
                return a + b;
            case SUBTRACT:
                return a - b;
        }
        return 0;
    }

    private static boolean chkLast(LinkedList<MathThing> dq) {
        if (dq.isEmpty()) return false;
        MathThing mt = dq.getLast();
        return mt instanceof MathResult;
    }

    private static boolean chkFst(LinkedList<MathThing> dq) {
        if (dq.isEmpty()) return true;
        MathThing mt = dq.getLast();
        return mt instanceof OperateSymbol || mt instanceof NonFunc;
    }

    private static boolean chkGpFst(LinkedList<MathThing> dq) {
        if (dq.isEmpty()) return true;
        boolean f = chkFst(dq);
        if (f) return true;
        MathThing mt = dq.getLast();
        return mt instanceof FuncSym;
    }

    private static boolean chkSn(LinkedList<MathThing> dq) {
        if (dq.isEmpty()) return true;
        MathThing mt1 = dq.getLast();
        boolean f = mt1 instanceof MathResult;
        if (f) return true;

        if (dq.size() == 1) return false;
        if (mt1 == OperateSymbol.ADD || mt1 == OperateSymbol.SUBTRACT) {
            dq.removeLast();
            MathThing mt2 = dq.getLast();
            dq.offerLast(mt1);
            return mt2 instanceof Num || mt2 instanceof Group;
        } else return true;
    }

    private interface MathThing {}

    private interface MathResult extends MathThing {
        Double cal();
    }

    private enum OperateSymbol implements MathThing {
        ADD, SUBTRACT, DIVIDE, MULTIPLIER, MOD, POW;

        int priority() {
            switch (this) {
                case ADD:
                case SUBTRACT:
                    return 1;
                case MOD:
                    return 2;
                case DIVIDE:
                case MULTIPLIER:
                    return 3;
                case POW:
                    return 4;
            }
            return 0;
        }
    }

    private enum NonFunc implements MathThing {
        GROUP_START, COMMA
    }

    private static abstract class FuncSym implements MathResult {
        LinkedList<MathThing> l;
        @Override
        public Double cal() {
            if (l == null) return null;
            int i = l.indexOf(NonFunc.COMMA);
            int li = l.lastIndexOf(NonFunc.COMMA);
            double d;
            int b;
            if (i < 0) {
                Double od = calListRecursion(l);
                if (od == null) return null;
                d = od;
                b = 0;
            } else if (i == li) {
                Double od = calListRecursion(new LinkedList<>(l.subList(0, i)));
                Double ob = calListRecursion(new LinkedList<>(l.subList(i + 1, l.size())));
                if (od == null || ob == null) return null;
                d = od;
                b = ob.intValue();
            } else return null;
            if (b == 0) return func(d);
            else {
                int ab = b < 0 ? -b : b;
                double f = 1;
                while (ab-->0) f *= 10;
                return b > 0 ? func(d / f) * f : func(d * f) / f;
            }
        }

        protected abstract double func(double a);
    }

    private static class FuncRound extends FuncSym {
        @Override
        protected double func(double a) {
            return Math.round(a);
        }
    }

    private static class FuncCeil extends FuncSym {
        @Override
        protected double func(double a) {
            return Math.ceil(a);
        }
    }

    private static class FuncFloor extends FuncSym {
        @Override
        protected double func(double a) {
            return Math.floor(a);
        }
    }

    private static class Num implements MathResult {
        double n;
        Num(double n) {
            this.n = n;
        }

        @Override
        public Double cal() {
            return n;
        }
    }

    private static class Group implements MathResult {
        LinkedList<MathThing> l;
        Group(LinkedList<MathThing> l) {
            this.l = l;
        }

        @Override
        public Double cal() {
            return calListRecursion(l);
        }
    }
}
