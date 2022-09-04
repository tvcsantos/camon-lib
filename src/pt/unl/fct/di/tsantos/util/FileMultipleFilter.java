package pt.unl.fct.di.tsantos.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

public class FileMultipleFilter implements FileFilter {
    private FileFilter[] filters;
    private boolean conjunction;

    public FileMultipleFilter(Collection<FileFilter> filters) {
        this(true, filters);
    }

    public FileMultipleFilter(FileFilter ... filters) {
        this(true, filters);
    }

    public FileMultipleFilter(boolean conjunction,
            Collection<FileFilter> filters) {
        this.filters = filters.toArray(new FileFilter[0]);
        this.conjunction = conjunction;
    }

    public FileMultipleFilter(boolean conjunction, FileFilter ... filters) {
        this.filters = filters;
        this.conjunction = conjunction;
    }

    public boolean accept(File pathname) {
        synchronized(this) {
            for (FileFilter filter : filters)
                if (conjunction && !filter.accept(pathname)) return false;
                else if (!conjunction && filter.accept(pathname)) return true;
            return true;
        }
    }
}
