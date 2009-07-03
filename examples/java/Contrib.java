import wrapper.util.concurrent.*;
import wrapper.lang.*;
class Counter {
  int value;
  Counter(int v)
    //@ requires true;
    //@ ensures result.value |-> v;
  {
    this.value=v;
  }
  void increment()
    //@ requires this.value |-> ?v;
    //@ ensures this.value |-> v + 1;
  {
    this.value = this.value + 1;
  }
}
/*@
box_class contrib_box(int contrib, handle owner) {
    invariant emp;

    action set_value(int contrib0);
        requires actionHandle == owner;
        ensures contrib == contrib0 && owner == old_owner;

    handle_predicate contrib_handle(int handleContrib) {
        invariant owner == predicateHandle && contrib == handleContrib;
        
        preserved_by set_value(contrib0) {}
    }
}

predicate_ctor sum(Counter c, box box1, box box2)()
    requires
        c.value |-> ?sum &*&
        contrib_handle(_, box1, ?contrib1) &*&
        contrib_handle(_, box2, sum - contrib1) &*&
        0 <= contrib1 &*& contrib1 <= 1 &*&
        0 <= sum - contrib1 &*& sum - contrib1 <= 1;

inductive contribute_info = contribute_info(box, box, box, Counter, Semaphore_);

predicate_family_instance thread_run_pre(Session.class)(Session session, contribute_info info)
    requires
        switch (info) {
            case contribute_info(box1, box2, thisBox, c, lock):
                return contribute_pre(session, box1, box2, thisBox, c, lock);
        };

predicate contribute_pre(Session session, box box1, box box2, box thisBox, Counter c, Semaphore_ l)
    requires
        session.counter |-> c &*& session.lock |-> l &*&
        [1/2]lock(l, sum(c,box1,box2)) &*& (thisBox == box1 || thisBox == box2) &*& contrib_box(thisBox, 0, _);

predicate_family_instance thread_run_post(Session.class)(Session session, contribute_info info)
    requires
        switch (info) {
            case contribute_info(box1, box2, thisBox, c, lock):
                return [1/2]lock(lock, sum(c, box1, box2)) &*& contrib_box(thisBox, 1, _);
        };

@*/
class Session implements Runnable{
  Counter counter;
  Semaphore_ lock;
  public Session(Counter c,Semaphore_ l)
  //@ requires c!=null;
  //@ ensures result.counter |-> c &*& result.lock |-> l;
  {
  this.counter=c;
  this.lock=l;
  }
  public void run()
    //@ requires thread_run_pre(Session.class)(this, ?info);
    //@ ensures thread_run_post(Session.class)(this, info);
  {
  //@ open thread_run_pre(Session.class)(this, _);
  //@ open contribute_pre(this, ?box1, ?box2, ?thisBox, _, _);
    Semaphore_ lock = this.lock;
    Counter c = this.counter;
    lock.acquire();
    //@ open sum(c, box1, box2)();
    //@ if (thisBox == box1) {} else {}
    /*@
    consuming_box_predicate contrib_box(thisBox, 0, _)
    consuming_handle_predicate contrib_handle(?thisHandle, _)
    perform_action set_value(1) {
        @*/
        {
            c.increment();
        }
        /*@
    }
    producing_box_predicate contrib_box(1, thisHandle)
    producing_handle_predicate contrib_handle(1);
    @*/
    //@ close sum(c, box1, box2)();
    lock.release();
    //@ close thread_run_post(Session.class)(this, contribute_info(box1, box2, thisBox, c, lock));
  }
}
class Program {
    public static void main(String[] args)
    //@ requires true;
    //@ ensures true;
    {
      Counter c=new Counter(0);
    /*@
    create_box box1 = contrib_box(0, handle1)
    and_handle handle1 = contrib_handle(0);
    @*/
    /*@
    create_box box2 = contrib_box(0, handle2)
    and_handle handle2 = contrib_handle(0);
    @*/
    //@ close sum(c, box1, box2)();
    //@ close create_lock_ghost_arg(sum(c, box1, box2));
	Semaphore_ lock=new Semaphore_(1);
    //@ split_fraction lock(lock, _) by 1/2;
    
	Session session1=new Session(c,lock);
    //@ close contribute_pre(session1, box1, box2, box1, c, lock);
    //@ close thread_run_pre(Session.class)(session1, contribute_info(box1, box2, box1, c, lock));
	Thread_ thread1=new Thread_(session1);
        thread1.start();
	Session session2=new Session(c,lock);
    //@ close contribute_pre(session2, box1, box2, box2, c, lock);
    //@ close thread_run_pre(Session.class)(session2, contribute_info(box1, box2, box2, c, lock));
	Thread_ thread2=new Thread_(session2);
        thread2.start();
    thread1.join();
    //@ open thread_run_post(Session.class)(session1, contribute_info(box1, box2, box1, c, lock));
    
    thread2.join();
    //@ open thread_run_post(Session.class)(session2, contribute_info(box1, box2, box2, c, lock));
    
    //@ merge_fractions lock(lock, _);
    //@ lock_dispose(lock);
    //@ open sum(c, box1, box2)();
    
    // The following perform_action statement is only to show contrib_handle(_, box1, 1)
    /*@
    consuming_box_predicate contrib_box(box1, 1, ?owner1)
    consuming_handle_predicate contrib_handle(?box1handle, _)
    perform_action set_value(1) {}
    producing_box_predicate contrib_box(1, owner1)
    producing_handle_predicate contrib_box_handle();
    @*/
    //@ dispose_box contrib_box(box1, _, _);
    //@ leak contrib_box_handle(_, box1);
    
    // The following perform_action statement is only to show contrib_handle(_, box2, 1)
    /*@
    consuming_box_predicate contrib_box(box2, 1, ?owner2)
    consuming_handle_predicate contrib_handle(?box2handle, _)
    perform_action set_value(1) {}
    producing_box_predicate contrib_box(1, owner2)
    producing_handle_predicate contrib_box_handle();
    @*/
    //@ dispose_box contrib_box(box2, _, _);
    //@ leak contrib_box_handle(_, box2);
    
    int sum = c.value;
    assert(sum == 2);
    }
}