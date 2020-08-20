package com.tml.sort.bubble;

public class BubbleSort {
	/**
	 *  ð������ԭ��ð������Ĺؼ�������Ԫ�������Ƚϲ�����λ�ã�ÿһ�ֵıȽϿ���ȷ��һ����������СԪ��
	 *  ð������ʱ�临�Ӷȣ�����Ҫ�ȽϵĴ���Ϊn(n-1)/2 �ռ�ռ����ΪO(1)
	 *  ����������¼�Ԫ����������Ҫ�ȽϵĴ�����Ԫ���ƶ��Ĵ������
	 *  
	 *  ��Ȼ���Զ�ð���������һ�����Ż������һ����־λ�����һ��ѭ��������û�з����κ�Ԫ��λ�õĽ�������������Ϊ���򣬽���ð�ݱȽ�
	 * @param array
	 */
	public int[] bubbleSort(int[] array)
	{
		int i,j,temp,len=array.length;
		boolean flag=true;
		for(i=0;i<len&&flag;i++)
		{
			flag = false; //�����һ��ѭ���иùؼ���ʼ��Ϊtrue����ô������Ϊ�������У�ѭ����ֹ
			for(j=len-1;j>i;j--)
			{
				if(array[j-1]>array[j])  //�Ƚϲ�����
				{
					temp = array[j-1];
					array[j-1] = array[j];
					array[j] = temp;
					flag = true;
				}
			}
		}
		return array;
	}
	/***
	 * ��ӡ������
	 */
	public  void print(int[] array )
	{
		for(int i=0;i<array.length;i++)
		{
			System.out.print(array[i]+",");
		}
	}
	
	public static void main(String[] args) {
		BubbleSort bs = new BubbleSort();
		int[] array = {1,5,9,3,4,18,7,6};
		bs.print(bs.bubbleSort(array));
	}
}
