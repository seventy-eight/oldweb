package org.seventyeight.database;

/**
 * @author cwolfgang
 *         Date: 21-11-12
 *         Time: 13:00
 */
public interface Query<RT> {
    public void execute();
    public RT get();
}
