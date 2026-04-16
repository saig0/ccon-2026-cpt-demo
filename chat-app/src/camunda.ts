import { Camunda8 } from '@camunda8/sdk';

const camunda = new Camunda8();
export const zbc = camunda.getZeebeGrpcApiClient();
