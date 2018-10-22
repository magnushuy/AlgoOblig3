package Oblig;

import java.util.*;


public class ObligSBinTre<T> implements Beholder<T>
{
    private static final class Node<T>   // en indre nodeklasse
    {
        private T verdi;                   // nodens verdi
        private Node<T> venstre, høyre;    // venstre og høyre barn
        private Node<T> forelder;          // forelder

        // konstruktør
        private Node(T verdi, Node<T> v, Node<T> h, Node<T> forelder)
        {
            this.verdi = verdi;
            venstre = v; høyre = h;
            this.forelder = forelder;
        }

        private Node(T verdi, Node<T> forelder)  // konstruktør
        {
            this(verdi, null, null, forelder);
        }

        @Override
        public String toString(){ return "" + verdi;}

    } // class Node

    private Node<T> rot;                            // peker til rotnoden
    private int antall;                             // antall noder
    private int endringer;                          // antall endringer

    private final Comparator<? super T> comp;       // komparator

    public ObligSBinTre(Comparator<? super T> c)    // konstruktør
    {
        rot = null;
        antall = 0;
        comp = c;
    }

    @Override
    public boolean leggInn(T verdi)
    {
        Objects.requireNonNull(verdi, "Ulovlig med nullverdier!");

        Node<T> p = rot, q = null;
        int cmp = 0;

        while (p != null)
        {
            q = p;
            cmp = comp.compare(verdi,p.verdi);
            p = cmp < 0 ? p.venstre : p.høyre;
        }


        p = new Node<>(verdi, null);

        if (q == null) rot = p;
        else if (cmp < 0){
            q.venstre = p;
            p.forelder = q;
        }
        else {
            q.høyre = p;
            p.forelder = q;
        }

        antall++;
        return true;
    }

    @Override
    public boolean inneholder(T verdi)
    {
        if (verdi == null) return false;

        Node<T> p = rot;

        while (p != null)
        {
            int cmp = comp.compare(verdi, p.verdi);
            if (cmp < 0) p = p.venstre;
            else if (cmp > 0) p = p.høyre;
            else return true;
        }

        return false;
    }

    @Override
    public boolean fjern(T verdi)
    { //Kopiert fra programkode 5.2.8
        if (verdi == null) return false;  // treet har ingen nullverdier

        Node<T> p = rot, q = null;   // q skal være forelder til p

        while (p != null)            // leter etter verdi
        {
            int cmp = comp.compare(verdi,p.verdi);      // sammenligner
            if (cmp < 0) { q = p; p = p.venstre; }      // går til venstre
            else if (cmp > 0) { q = p; p = p.høyre; }   // går til høyre
            else break;    // den søkte verdien ligger i p
        }
        if (p == null) return false;   // finner ikke verdi

        if (p.venstre == null || p.høyre == null)  // Tilfelle 1) og 2)
        {
            Node<T> b = p.venstre != null ? p.venstre : p.høyre;  // b for barn
            if(b != null) b.forelder = q;
            if (p == rot) rot = b;
            else if (p == q.venstre){
                q.venstre = b;
            }
            else {
                q.høyre = b;
            }
        }
        else  // Tilfelle 3)
        {
            Node<T> s = p, r = p.høyre;   // finner neste i inorden
            while (r.venstre != null)
            {
                s = r;    // s er forelder til r
                r = r.venstre;
            }

            p.verdi = r.verdi;   // kopierer verdien i r til p

            if(r.høyre != null) r.høyre.forelder = s;

            if (s != p) s.venstre = r.høyre;
            else s.høyre = r.høyre;
        }

        antall--;   // det er nå én node mindre i treet
        endringer++;
        return true;
    }

    public int fjernAlle(T verdi)
    {
        int antFjernet = 0;
        while(fjern(verdi)) antFjernet++;
        return antFjernet;
    }

    @Override
    public int antall()
    {
        return antall;
    }

    public int antall(T verdi)
    {
        if(verdi == null){
            return 0;
        }

        Node<T> p = rot;
        int a = 0;

        while (p != null){
            int cmp = comp.compare(verdi, p.verdi);
            if(cmp < 0) p = p.venstre;
            else{
                if(cmp == 0) a++;
                p = p.høyre;
            }
        }

        return a;

    }

    @Override
    public boolean tom()
    {
        return antall == 0;
    }

    @Override
    public void nullstill()
    {
        if(!tom()) nullstill(rot);
        rot = null;
        antall = 0;
        endringer++;
    }

    private static <T> void nullstill(Node<T> p)
    {
        if(p.venstre != null){
            nullstill(p.venstre);
            p.venstre = null;
        }
        if(p.høyre != null){
            nullstill(p.høyre);
            p.høyre = null;
        }
        p.verdi = null;
    }

    private static <T> Node<T> nesteInorden(Node<T> p)
    {
        if(p.høyre != null){ //Hvis p har et høyre subtre
            p = p.høyre; //Går til høyre subtre
            while(p.venstre != null) p= p.venstre; //Går til noden lengst ned til venstr ei subtreet
        }
        else{
            while(p.forelder != null && p == p.forelder.høyre){ //Hvis noden er nest sist i inorden
                p = p.forelder;
            }
            p = p.forelder;
        }
        return p;
    }

    @Override
    public String toString()
    {
        if(tom()) return "[]";

        StringJoiner sj = new StringJoiner(", ", "[" , "]");

        Node<T> p = rot;
        while(p.venstre != null) p = p.venstre; //Går til noden lengst nede til venstre som er første node i inorden

        while(p != null){
            sj.add(p.verdi.toString());
            p = nesteInorden(p);
        }
        return sj.toString();
    }

    public String omvendtString()
    {
        if(tom()) return "[]";
        StringJoiner sj = new StringJoiner(", ", "[" , "]");
        ArrayDeque<Node<T>> stakk = new ArrayDeque<>();

        Node<T> p = rot;

        while(p.høyre != null){
            stakk.addLast(p); //Legger til alle verdiene til høyre inn i stakken utenom siste
            p = p.høyre;
        }

        sj.add(p.verdi.toString()); //Legger til første verdien i motsatt inorden i return-string

        while(true){ //Kjør til break
            if(p.venstre != null){
                p = p.venstre;
                while(p.høyre != null){
                    stakk.addLast(p);
                    p = p.høyre;
                }
            }
            else if(!stakk.isEmpty()) p = stakk.removeLast();
            else break;

            sj.add(p.verdi.toString());
        }
        return sj.toString();
    }

    public String høyreGren()
    {
        if(tom()) return "[]";

        StringJoiner sj = new StringJoiner(", ", "[", "]");
        Node<T> p = rot;
        while(true){
            sj.add(p.verdi.toString());
            if(p.høyre != null) p = p.høyre;
            else if(p.venstre != null) p = p.venstre;
            else break;
        }

        return sj.toString();
    }

    public String lengstGren()
    {
        if(tom()) return "[]";

        ArrayDeque<Node<T>> queue = new ArrayDeque<>();
        queue.add(rot);

        Node<T> p = null;

        while(!queue.isEmpty()){
            p = queue.removeFirst(); //Fjerner den første i køen
            if(p.høyre != null) queue.addLast(p.høyre); //Legger til høyre til bakerst i køen
            if(p.venstre != null) queue.addLast(p.venstre); //Legger til venstre bakerst i køen
        }

        ArrayDeque<T> stakk = new ArrayDeque<>(); //Lager en stakk som vi kan skrive ut
        while(p != null){
            stakk.addFirst(p.verdi); //Legger til verdien til p først i køen. Første verdi i stakken blir da rot, siste blir verdien til siste bladnode
            p = p.forelder; //Går oppover i treet
        }
        return stakk.toString();
    }

    public String[] grener()
    {
        throw new UnsupportedOperationException("Ikke kodet ennå!");
    }

    public String bladnodeverdier()
    {
        throw new UnsupportedOperationException("Ikke kodet ennå!");
    }

    public String postString()
    {
        throw new UnsupportedOperationException("Ikke kodet ennå!");
    }

    @Override
    public Iterator<T> iterator()
    {
        return new BladnodeIterator();
    }

    private class BladnodeIterator implements Iterator<T>
    {
        private Node<T> p = rot, q = null;
        private boolean removeOK = false;
        private int iteratorendringer = endringer;

        private BladnodeIterator()  // konstruktør
        {
            throw new UnsupportedOperationException("Ikke kodet ennå!");
        }

        @Override
        public boolean hasNext()
        {
            return p != null;  // Denne skal ikke endres!
        }

        @Override
        public T next()
        {
            throw new UnsupportedOperationException("Ikke kodet ennå!");
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Ikke kodet ennå!");
        }

    } // BladnodeIterator

} // ObligSBinTre