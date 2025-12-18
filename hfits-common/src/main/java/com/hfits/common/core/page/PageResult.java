package com.hfits.common.core.page;

import java.io.Serializable;
import java.util.List;

/**
 * ��ҳ�������
 *
 * @author hfits
 */
public class PageResult<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** �ܼ�¼�� */
    private long total;

    /** �б����� */
    private List<T> list;

    /**
     * ��ҳ�������
     */
    public PageResult()
    {
    }

    /**
     * ��ҳ�������
     *
     * @param list �б�����
     * @param total �ܼ�¼��
     */
    public PageResult(List<T> list, long total)
    {
        this.list = list;
        this.total = total;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public List<T> getList()
    {
        return list;
    }

    public void setList(List<T> list)
    {
        this.list = list;
    }
}
